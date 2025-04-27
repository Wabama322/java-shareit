package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingForResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.booking.model.StateBooking;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.Status.REJECTED;
import static ru.practicum.shareit.booking.model.Status.WAITING;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public BookingForResponse addBooking(long userId, BookingDtoRequest bookingDtoRequest) {
        try {
            Item item = itemRepository.findById(bookingDtoRequest.getItemId())
                    .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

            if (!item.getAvailable()) {
                throw new BadRequestException("Вещь недоступна");
            }

            if (item.getOwner().getId().equals(user.getId())) {
                throw new BadRequestException("Нельзя бронировать свою вещь");
            }

            if (bookingDtoRequest.getStart().isAfter(bookingDtoRequest.getEnd())) {
                throw new BadRequestException("Некорректные даты бронирования");
            }

            List<Booking> bookings = bookingRepository.findByItemIdAndStartLessThanEqualAndEndGreaterThanEqual(
                    item.getId(), bookingDtoRequest.getStart(), bookingDtoRequest.getEnd(), Sort.unsorted());
            if (!bookings.isEmpty()) {
                throw new RuntimeException("Вещь уже забронирована на указанные даты");
            }

            Booking booking = BookingMapper.toBooking(bookingDtoRequest, item, user);
            booking.setStatus(WAITING);
            bookingRepository.save(booking);

            return BookingMapper.toBookingForResponseMapper(booking);
        } catch (NotFoundException e) {
            throw e; // 404
        } catch (BadRequestException e) {
            throw e; // 400
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании бронирования", e); // 500
        }
    }

     public void validateBooking(BookingDtoRequest bookingDtoRequest, Item item, User booker) {
        if (item.getOwner().getId().equals(booker.getId())) {
            throw new AccessDeniedException("Нельзя бронировать свою вещь");
        }

        if (bookingDtoRequest.getStart().isAfter(bookingDtoRequest.getEnd())) {
            throw new BadRequestException("Некорректные даты бронирования");
        }

        List<Booking> bookings = bookingRepository.findByItemIdAndStartLessThanEqualAndEndGreaterThanEqual(
                item.getId(), bookingDtoRequest.getStart(), bookingDtoRequest.getEnd(), Sort.unsorted());
        if (!bookings.isEmpty()) {
            throw new BadRequestException("Пересечение бронирований");
        }
    }

    @Transactional
    @Override
    public BookingForResponse updateBooking(long bookingId, long userId, Boolean approved) {
        Booking booking = checkBooking(bookingId);
        Item item = booking.getItem();
        if (item.getOwner().getId() != userId) {
            throw new AccessDeniedException("Пользователь не является владельцем вещи ");
        }
        if (!booking.getStatus().equals(WAITING)) {
            throw new BadRequestException("Данное бронирование уже внесено и имеет статус "
                    + booking.getStatus());
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }
        return BookingMapper.toBookingForResponseMapper(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingForResponse getBooking(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getBooker().getId() == userId || b.getItem().getOwner().getId() == userId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено или доступ запрещен"));
        return BookingMapper.toBookingForResponseMapper(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingForResponse> getAllBookingByUser(String state, long userId, int from, int size) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = new ArrayList<>();
        StateBooking stateBooking = StateBooking.getStateFromText(state);
        Pageable pageable = PageRequest.of(from / size, size);

        switch (stateBooking) {
            case ALL:
                result = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                break;
            case CURRENT:
                result = bookingRepository.findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                        userId, now, now, pageable);
                break;
            case PAST:
                result = bookingRepository.findByBookerIdAndEndBeforeAndStatusOrderByStartDesc(
                        userId, now, Status.APPROVED, pageable);
                break;
            case FUTURE:
                result = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                break;
            case WAITING:
                result = bookingRepository.findWaitingBookingsByBooker(userId, pageable);
                break;
            case REJECTED:
                result = bookingRepository.findRejectedBookingsByBooker(userId, pageable);
                break;
        }

        return result.stream()
                .map(BookingMapper::toBookingForResponseMapper)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingForResponse> getAllBookingByOwner(String state, long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (from < 0 || size <= 0) {
            throw new BadRequestException("Некорректные параметры пагинации");
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
            StateBooking bookingState = StateBooking.fromString(state);

            switch (bookingState) {
                case ALL:
                    return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable)
                            .stream()
                            .map(BookingMapper::toBookingForResponseMapper)
                            .collect(Collectors.toList());

                case CURRENT:
                    return bookingRepository.findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                                    userId, now, now, pageable)
                            .stream()
                            .map(BookingMapper::toBookingForResponseMapper)
                            .collect(Collectors.toList());

                case PAST:
                    return bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusOrderByStartDesc(
                                    userId, now, Status.APPROVED, pageable)
                            .stream()
                            .map(BookingMapper::toBookingForResponseMapper)
                            .collect(Collectors.toList());

                case FUTURE:
                    return bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(
                                    userId, now, pageable)
                            .stream()
                            .map(BookingMapper::toBookingForResponseMapper)
                            .collect(Collectors.toList());

                case WAITING:
                    return bookingRepository.findWaitingBookingsByOwner(userId, pageable)
                            .stream()
                            .map(BookingMapper::toBookingForResponseMapper)
                            .collect(Collectors.toList());

                case REJECTED:
                    return bookingRepository.findRejectedBookingsByOwner(userId, pageable)
                            .stream()
                            .map(BookingMapper::toBookingForResponseMapper)
                            .collect(Collectors.toList());

                default:
                    throw new BadRequestException("Неизвестное состояние: " + state);
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении бронирований владельца", e);
        }
    }

    private Booking checkBooking(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не зарегистрировано"));
    }
}
