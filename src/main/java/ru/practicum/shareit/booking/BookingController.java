package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.utill.Constants;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService service;

    @PostMapping
    public BookingDtoResponse addBooking(@RequestHeader(Constants.USER_HEADER) long userId,
                                         @Valid @RequestBody BookingDtoRequest bookingDtoRequest) {
        log.info("POST запрос на создание бронирования от пользователя ID: {}", userId);
        return service.addBooking(userId, bookingDtoRequest);
    }

    @PatchMapping("/{booking-id}")
    public BookingDtoResponse updateBooking(@PathVariable("booking-id") long bookingId,
                                            @RequestHeader(Constants.USER_HEADER) long userId,
                                            @RequestParam Boolean approved) {
        log.info("PATCH запрос на обновление бронирования ID: {} от пользователя ID: {}", bookingId, userId);
        return service.updateBooking(bookingId, userId, approved);
    }

    @GetMapping("/{booking-id}")
    public BookingDtoResponse getBooking(@PathVariable("booking-id") long bookingId,
                                         @RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("GET запрос на получение бронирования ID: {} от пользователя ID: {}", bookingId, userId);
        return service.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoResponse> getAllBookingByUser(@RequestParam(defaultValue = "ALL") String state,
                                                        @RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("GET запрос на получение бронирований пользователя ID: {} со статусом {}", userId, state);
        return service.getAllBookingByUser(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> getAllBookingByOwner(@RequestParam(defaultValue = "ALL") String state,
                                                         @RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("GET запрос на получение бронирований владельца ID: {} со статусом {}", userId, state);
        return service.getAllBookingByOwner(state, userId);
    }
}
