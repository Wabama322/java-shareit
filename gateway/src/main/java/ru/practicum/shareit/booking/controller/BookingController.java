package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.utill.Constants;

/**
 * TODO Sprint add-bookings.
 */

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    static final String PATH = "/{booking-id}";

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(Constants.USER_HEADER) long userId,
                                             @RequestBody @Valid BookingDtoRequest requestDto) {
        log.info("POST запрос на создание бронирования {}, userId={}", requestDto, userId);
        return bookingClient.addBooking(userId, requestDto);
    }

    @PatchMapping(PATH)
    public ResponseEntity<Object> updateBooking(@PathVariable("booking-id") Long bookingId,
                                                @RequestHeader(Constants.USER_HEADER) Long ownerId,
                                                @RequestParam(name = "approved") boolean approved) {
        log.info("PATCH запрос на обновление бронирования userId={} bookingId={}", ownerId, bookingId);
        return bookingClient.updateBooking(bookingId, ownerId, approved);
    }

    @GetMapping(PATH)
    public ResponseEntity<Object> getBooking(@RequestHeader(Constants.USER_HEADER) long userId,
                                             @PathVariable("booking-id") Long bookingId) {
        log.info("GET запрос на получение бронирования с id={}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingByUser(@RequestHeader(Constants.USER_HEADER) long userId,
                                                      @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                      @Positive @RequestParam(name = "size", defaultValue = "20") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new UnsupportedStatusException("Неизвестный статус: " + stateParam));
        log.info("GET запрос на получение всех бронирований state {}, userId={}, from={}, size={}", stateParam, userId,
                from, size);
        return bookingClient.getAllBookingByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsByOwner(@RequestParam(defaultValue = "ALL") String state,
                                                        @RequestHeader(Constants.USER_HEADER) Long userId,
                                                        @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                        @Positive @RequestParam(defaultValue = "20") Integer size) {
        BookingState stateParam = BookingState.from(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
        log.info("GET запрос на получение бронирований владельца userId={},state {}, from={}, size={}", userId, state,
                from, size);
        return bookingClient.getAllBookingByOwner(userId, stateParam, from, size);
    }
}
