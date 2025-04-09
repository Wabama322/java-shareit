package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

public interface BookingService {
    BookingDtoResponse addBooking(long userId, BookingDtoRequest bookingDtoRequest);

    BookingDtoResponse updateBooking(long bookingId, long userId, Boolean approved);

    BookingDtoResponse getBooking(long bookingId, long userId);

    List<BookingDtoResponse> getAllBookingByUser(String state, long userId);

    List<BookingDtoResponse> getAllBookingByOwner(String state, long userId);
}
