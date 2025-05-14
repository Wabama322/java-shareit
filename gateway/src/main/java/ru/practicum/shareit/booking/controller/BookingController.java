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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.utill.Constants;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private static final String PATH = "/{bookingId}";
    private static final String DEFAULT_STATE = "ALL";
    private static final String DEFAULT_FROM = "0";
    private static final String DEFAULT_SIZE = "10";

    private final UserClient userClient;
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @RequestBody @Valid BookingDtoRequest bookingDtoRequest) {
        log.debug("Creating booking for userId={}", userId);
        return bookingClient.addBooking(userId, bookingDtoRequest);
    }

    @PatchMapping(PATH)
    public ResponseEntity<Object> approveBooking(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @PathVariable long bookingId,
            @RequestParam boolean approved) {
        log.debug("Updating booking status: bookingId={}, userId={}, approved={}",
                bookingId, userId, approved);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping(PATH)
    public ResponseEntity<Object> getBooking(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @PathVariable long bookingId) {
        log.debug("Fetching booking: bookingId={}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsByUser(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @RequestParam(defaultValue = DEFAULT_STATE) String state,
            @PositiveOrZero @RequestParam(defaultValue = DEFAULT_FROM) int from,
            @Positive @RequestParam(defaultValue = DEFAULT_SIZE) int size) {

        BookingState bookingState = parseBookingState(state);
        log.debug("Fetching user bookings: userId={}, state={}, from={}, size={}",
                userId, state, from, size);

        return bookingClient.getAllBookingsByUser(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsByOwner(
            @RequestHeader(Constants.USER_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "20") Integer size) {

        ResponseEntity<Object> userResponse = userClient.getUserById(userId);
        if (userResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));

        return bookingClient.getAllBookingsByOwner(userId, bookingState, from, size);
    }

    private BookingState parseBookingState(String state) {
        return BookingState.from(state)
                .orElseThrow(() -> {
                    log.error("Invalid booking state provided: {}", state);
                    return new UnsupportedStatusException("Unknown state: " + state);
                });
    }

    private void validateUserExists(long userId) {
        ResponseEntity<Object> userResponse = userClient.getUserById(userId);
        if (userResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.warn("User not found with id: {}", userId);
            throw new NotFoundException("User not found");
        }
    }
}