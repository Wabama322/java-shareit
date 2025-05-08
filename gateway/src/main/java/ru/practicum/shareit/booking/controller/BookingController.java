package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.utill.Constants;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final UserClient userClient;
    private final BookingClient bookingClient;
    private static final String PATH = "/{bookingId}";

    @PostMapping
    public ResponseEntity<Object> addBooking(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @RequestBody @Valid BookingDtoRequest bookingDtoRequest) {
        log.info("Creating booking {}, userId={}", bookingDtoRequest, userId);
        return bookingClient.addBooking(userId, bookingDtoRequest);
    }

    @PatchMapping(PATH)
    public ResponseEntity<Object> approveBooking(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @PathVariable("bookingId") long bookingId,
            @RequestParam boolean approved) {
        log.info("Updating booking {}, userId={}, approved={}", bookingId, userId, approved);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping(PATH)
    public ResponseEntity<Object> getBooking(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @PathVariable("bookingId") long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsByUser(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size) {
        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
        log.info("Get bookings with state {}, userId={}, from={}, size={}", state, userId, from, size);
        return bookingClient.getAllBookingsByUser(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsByOwner(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size) {

        ResponseEntity<Object> userResponse = userClient.getUserById(userId);
        if (userResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.warn("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> {
                    log.error("Invalid booking state: {}", state);
                    throw new UnsupportedStatusException("Unknown state: " + state);
                });

        log.info("Requesting bookings for owner: userId={}, state={}, from={}, size={}",
                userId, state, from, size);

        return bookingClient.getAllBookingsByOwner(userId, bookingState, from, size);
    }
}