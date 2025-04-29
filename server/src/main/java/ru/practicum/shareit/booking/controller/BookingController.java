package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingForResponse;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.utill.Constants;

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
    static final String path = "/{booking-id}";

    @PostMapping
    public BookingForResponse addBooking(@RequestHeader(Constants.USER_HEADER) long userId,
                                         @RequestBody BookingDtoRequest bookingDtoRequest) {
        log.info("POST запрос на создание бронирования");
        return service.addBooking(userId, bookingDtoRequest);
    }

    @PatchMapping(path)
    public BookingForResponse updateBooking(@PathVariable("booking-id") long bookingId,
                                            @RequestHeader(Constants.USER_HEADER) long userId,
                                            @RequestParam Boolean approved) {
        log.info("PATCH запрос на обновление бронирования");
        return service.updateBooking(bookingId, userId, approved);
    }

    @GetMapping(path)
    public BookingForResponse getBooking(@PathVariable("booking-id") long bookingId,
                                         @RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("GET запрос на получение бронирования");
        return service.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingForResponse> getAllBookingByUser(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader(Constants.USER_HEADER) long userId,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("GET запрос на получение списка бронирований user с Id {} со статусом {}", userId, state);
        return service.getAllBookingByUser(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingForResponse> getAllBookingByOwner(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestHeader(Constants.USER_HEADER) long userId,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("GET запрос на получение бронирования  owner с Id {} со статусом {}", userId, state);
        return service.getAllBookingByOwner(state, userId, from, size);
    }
}
