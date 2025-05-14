package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingForResponse;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.utill.Constants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final String bookingIdPathVariable = "booking-id";
    private final String bookingIdPath = "/{" + bookingIdPathVariable + "}";
    private final List<String> allowedStates = List.of(
            "ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"
    );
    private final String defaultState = "ALL";
    private final BookingService service;

    @PostMapping
    public ResponseEntity<BookingForResponse> addBooking(
            @RequestHeader(Constants.USER_HEADER) @Positive long userId,
            @RequestBody @Valid BookingDtoRequest bookingDtoRequest) {
        log.info("POST запрос на создание бронирования от пользователя {}", userId);
        return ResponseEntity.ok(service.addBooking(userId, bookingDtoRequest));
    }

    @PatchMapping(bookingIdPath)
    public ResponseEntity<BookingForResponse> updateBooking(
            @PathVariable(bookingIdPathVariable) @Positive long bookingId,
            @RequestHeader(Constants.USER_HEADER) @Positive long userId,
            @RequestParam Boolean approved) {
        log.info("PATCH запрос на обновление статуса бронирования {} от пользователя {}", bookingId, userId);
        return ResponseEntity.ok(service.updateBooking(bookingId, userId, approved));
    }

    @GetMapping(bookingIdPath)
    public ResponseEntity<BookingForResponse> getBooking(
            @PathVariable(bookingIdPathVariable) @Positive long bookingId,
            @RequestHeader(Constants.USER_HEADER) @Positive long userId) {
        log.info("GET запрос на получение бронирования {} от пользователя {}", bookingId, userId);
        return ResponseEntity.ok(service.getBooking(bookingId, userId));
    }

    @GetMapping
    public ResponseEntity<List<BookingForResponse>> getAllBookingByUser(
            @RequestParam(defaultValue = defaultState) String state,
            @RequestHeader(Constants.USER_HEADER) @Positive long userId,
            @RequestParam(name = "from", defaultValue = "#{T(java.lang.Integer).MAX_VALUE}") int from,
            @RequestParam(name = "size", defaultValue = "20") @Positive int size) {
        validateState(state);
        log.info("GET запрос на получение списка бронирований пользователя {} со статусом {}", userId, state);
        return ResponseEntity.ok(service.getAllBookingByUser(state, userId, from, size));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingForResponse>> getAllBookingByOwner(
            @RequestParam(defaultValue = defaultState) String state,
            @RequestHeader(Constants.USER_HEADER) @Positive long userId,
            @RequestParam(name = "from", defaultValue = "#{T(java.lang.Integer).MAX_VALUE}") int from,
            @RequestParam(name = "size", defaultValue = "20") @Positive int size) {
        validateState(state);
        log.info("GET запрос на получение бронирований владельца {} со статусом {}", userId, state);
        return ResponseEntity.ok(service.getAllBookingByOwner(state, userId, from, size));
    }

    private void validateState(String state) {
        if (!allowedStates.contains(state)) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}