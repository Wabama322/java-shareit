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
import ru.practicum.shareit.booking.model.StateBooking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
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
        Item item = itemRepository.findById(bookingDtoRequest.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        validateBooking(bookingDtoRequest, item, user);

        Booking booking = BookingMapper.toBooking(bookingDtoRequest, item, user);
        booking.setStatus(WAITING);
        bookingRepository.save(booking);

        return BookingMapper.toBookingForResponseMapper(booking);
    }

    private void validateBooking(BookingDtoRequest bookingDtoRequest, Item item, User booker) {
        if (item.getOwner().getId().equals(booker.getId())) {
            throw new BadRequestException("Нельзя бронировать свою вещь");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна");
        }
        if (bookingDtoRequest.getStart().isAfter(bookingDtoRequest.getEnd())) {
            throw new BadRequestException("Некорректные даты бронирования");
        }
        if (hasBookingConflicts(item.getId(), bookingDtoRequest.getStart(), bookingDtoRequest.getEnd())) {
            throw new BadRequestException("Вещь уже забронирована на указанные даты");
        }
    }

    private boolean hasBookingConflicts(long itemId, LocalDateTime start, LocalDateTime end) {
        return !bookingRepository.findByItemIdAndStartLessThanEqualAndEndGreaterThanEqual(
                itemId, start, end, Sort.unsorted()).isEmpty();
    }

    @Transactional
    @Override
    public BookingForResponse updateBooking(long bookingId, long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (booking.getItem().getOwner().getId() != userId) {
            throw new AccessDeniedException("Пользователь не является владельцем вещи");
        }
        if (!booking.getStatus().equals(WAITING)) {
            throw new BadRequestException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? Status.APPROVED : REJECTED);
        return BookingMapper.toBookingForResponseMapper(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingForResponse getBooking(long bookingId, long userId) {
        return bookingRepository.findById(bookingId)
                .filter(b -> b.getBooker().getId() == userId || b.getItem().getOwner().getId() == userId)
                .map(BookingMapper::toBookingForResponseMapper)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено или доступ запрещен"));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingForResponse> getAllBookingByUser(String state, long userId, int from, int size) {
        validatePagination(from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));

        return StateBooking.from(state)
                .map(stateBooking -> getBookingsForState(userId, stateBooking, pageable))
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
    }

    private List<BookingForResponse> getBookingsForState(long userId, StateBooking state, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                        userId, now, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeAndStatusOrderByStartDesc(
                        userId, now, Status.APPROVED, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, REJECTED, pageable);
                break;
            default:
                throw new IllegalStateException("Неизвестное состояние: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingForResponseMapper)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingForResponse> getAllBookingByOwner(String state, long userId, int from, int size) {
        validatePagination(from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));

        return StateBooking.from(state)
                .map(stateBooking -> getOwnerBookingsForState(userId, stateBooking, pageable))
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
    }

    private List<BookingForResponse> getOwnerBookingsForState(long userId, StateBooking state, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                        userId, now, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusOrderByStartDesc(
                        userId, now, Status.APPROVED, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, REJECTED, pageable);
                break;
            default:
                throw new IllegalStateException("Неизвестное состояние: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingForResponseMapper)
                .collect(Collectors.toList());
    }

    private void validatePagination(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Некорректные параметры пагинации");
        }
    }
}
