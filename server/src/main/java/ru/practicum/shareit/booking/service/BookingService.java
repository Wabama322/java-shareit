package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingForResponse;

import java.util.List;

public interface BookingService {
    BookingForResponse addBooking(long userId, BookingDtoRequest bookingDtoRequest);

    BookingForResponse updateBooking(long bookingId, long userId, Boolean approved);

    BookingForResponse getBooking(long bookingId, long userId);

    List<BookingForResponse> getAllBookingByUser(String state, long userId, int from, int size);

    List<BookingForResponse> getAllBookingByOwner(String state, long userId, int from, int size);
}