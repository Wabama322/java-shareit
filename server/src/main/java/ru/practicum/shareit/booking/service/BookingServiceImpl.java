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

import static ru.practicum.shareit.booking.model.Status.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final Sort SORT_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public BookingForResponse addBooking(long userId, BookingDtoRequest bookingDtoRequest) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDtoRequest.getItemId());

        validateBookingCreation(bookingDtoRequest, item, booker);

        Booking booking = BookingMapper.toBooking(bookingDtoRequest, item, booker);
        booking.setStatus(WAITING);
        return BookingMapper.toBookingForResponse(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingForResponse updateBooking(long bookingId, long userId, Boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getItem().getOwner().getId() != userId) {
            throw new AccessDeniedException("Только владелец может подтверждать бронирование");
        }
        if (booking.getStatus() != WAITING) {
            throw new BadRequestException("Статус уже изменен");
        }

        booking.setStatus(approved ? APPROVED : REJECTED);
        return BookingMapper.toBookingForResponse(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingForResponse getBooking(long bookingId, long userId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException("Доступ запрещен");
        }

        return BookingMapper.toBookingForResponse(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingForResponse> getAllBookingByUser(String state, long userId, int from, int size) {
        validateUserExists(userId);
        validatePagination(from, size);

        StateBooking stateEnum = parseState(state);
        Pageable pageable = PageRequest.of(from / size, size, SORT_START_DESC);

        return mapToDto(getUserBookings(stateEnum, userId, pageable));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingForResponse> getAllBookingByOwner(String state, long userId, int from, int size) {
        validateUserExists(userId);
        validatePagination(from, size);

        StateBooking stateEnum = parseState(state);
        Pageable pageable = PageRequest.of(from / size, size, SORT_START_DESC);

        return mapToDto(getOwnerBookings(stateEnum, userId, pageable));
    }

    private List<Booking> getUserBookings(StateBooking state, long userId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
            case CURRENT:
                return bookingRepository.findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                        userId, now, now, pageable);
            case PAST:
                return bookingRepository.findByBookerIdAndEndBeforeAndStatusOrderByStartDesc(
                        userId, now, APPROVED, pageable);
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
            case WAITING:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, WAITING, pageable);
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, REJECTED, pageable);
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    private List<Booking> getOwnerBookings(StateBooking state, long userId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
            case CURRENT:
                return bookingRepository.findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                        userId, now, now, pageable);
            case PAST:
                return bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusOrderByStartDesc(
                        userId, now, APPROVED, pageable);
            case FUTURE:
                return bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, WAITING, pageable);
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, REJECTED, pageable);
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    private void validateBookingCreation(BookingDtoRequest dto, Item item, User booker) {
        if (item.getOwner().equals(booker)) {
            throw new BadRequestException("Владелец не может бронировать свою вещь");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }
        if (dto.getStart().isAfter(dto.getEnd())) {
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }
        if (!bookingRepository.findByItemIdAndStartLessThanEqualAndEndGreaterThanEqual(
                item.getId(), dto.getEnd(), dto.getStart(), Sort.unsorted()).isEmpty()) {
            throw new BadRequestException("Конфликт бронирований");
        }
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Item getItemOrThrow(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }

    private Booking getBookingOrThrow(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
    }

    private void validateUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private void validatePagination(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Некорректные параметры пагинации");
        }
    }

    private StateBooking parseState(String state) {
        return StateBooking.from(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
    }

    private List<BookingForResponse> mapToDto(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingForResponse)
                .toList();
    }
}