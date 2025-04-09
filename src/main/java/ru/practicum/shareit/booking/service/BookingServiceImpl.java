package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.StateBooking;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public BookingDtoResponse addBooking(long userId, BookingDtoRequest bookingDtoRequest) {
        Item item = itemRepository.findById(bookingDtoRequest.getItemId()).orElseThrow(() ->
                new NotFoundException("Вещь с id " +
                        bookingDtoRequest.getItemId() + " не найдена"));
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь не доступна для бронирования");
        }
        User user = checkUser(userId);
        validateBooking(bookingDtoRequest, item, user);
        Booking booking = BookingMapper.toBooking(bookingDtoRequest, item, user);
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(user);
        Booking result = bookingRepository.save(booking);
        return BookingMapper.toBookingForResponseMapper(result);
    }

    @Override
    public BookingDtoResponse updateBooking(long bookingId, long userId, Boolean approved) {
        Booking booking = checkBooking(bookingId);
        Item item = booking.getItem();
        if (item.getOwner().getId() != userId) {
            throw new ValidationException("Пользователь не является владельцем вещи ");
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Данное бронирование уже внесено и имеет статус "
                    + booking.getStatus());
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        checkUser(userId);
        return BookingMapper.toBookingForResponseMapper(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDtoResponse getBooking(long bookingId, long userId) {
        checkUser(userId);
        checkBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).filter(booking1 ->
                booking1.getBooker().getId() == userId
                        || booking1.getItem().getOwner().getId() == userId).orElseThrow(() ->
                new NotFoundException("Пользователь не является владельцем вещи "));
        ;
        return BookingMapper.toBookingForResponseMapper(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDtoResponse> getAllBookingByUser(String state, long userId) {
        checkUser(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = new ArrayList<>();
        StateBooking stateBooking = StateBooking.getStateFromText(state);

        switch (stateBooking) {
            case ALL:
                result = bookingRepository.findAllBookingsByBooker(userId);
                break;
            case CURRENT:
                result = bookingRepository.findAllCurrentBookingsByBooker(userId, now);
                break;
            case PAST:
                result = bookingRepository.findAllPastBookingsByBooker(userId, now, Status.APPROVED);
                break;
            case FUTURE:
                result = bookingRepository.findAllFutureBookingsByBooker(userId, now);
                break;
            case WAITING:
                result = bookingRepository.findAllWaitingBookingsByBooker(userId, Status.WAITING);
                break;
            case REJECTED:
                result = bookingRepository.findAllBookingsByBooker(userId, Status.REJECTED, Status.CANCELED);
                break;
        }

        return result.stream().map(BookingMapper::toBookingForResponseMapper)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDtoResponse> getAllBookingByOwner(String state, long userId) {
        checkUser(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = new ArrayList<>();
        StateBooking stateBooking = StateBooking.getStateFromText(state);

        switch (stateBooking) {
            case ALL:
                result = bookingRepository.findAllBookingsOwner(userId);
                break;
            case CURRENT:
                result = bookingRepository.findAllCurrentBookingsByOwner(userId, now);
                break;
            case PAST:
                result = bookingRepository.findAllPastBookingsByOwner(userId, now, Status.APPROVED);
                break;
            case FUTURE:
                result = bookingRepository.findAllFutureBookingsByOwner(userId, now);
                break;
            case WAITING:
                result = bookingRepository.findAllWaitingBookingsByOwner(userId, Status.WAITING);
                break;
            case REJECTED:
                result = bookingRepository.findAllBookingsByOwner(userId, Status.REJECTED, Status.CANCELED);
                break;
        }

        return result.stream().map(BookingMapper::toBookingForResponseMapper)
                .collect(Collectors.toList());
    }

    private User checkUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " +
                        userId + " не зарегистрирован"));
    }

    private Booking checkBooking(long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с id " +
                        bookingId + " не зарегистрировано"));
    }

    private void validateBooking(BookingDtoRequest bookingDtoRequest, Item item, User booker) {
        if (item.getOwner().getId().equals(booker.getId())) {
            throw new ValidationException("Нельзя забронировать свою вещь");
        }
        List<Booking> bookings = bookingRepository.checkValidateBookings(item.getId(), bookingDtoRequest.getStart());
        if (bookings != null && !bookings.isEmpty()) {
            throw new BadRequestException("Найдено пересечение бронирований на вещь " + item.getName());
        }
    }
}