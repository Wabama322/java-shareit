package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.exception.BadRequestException;

import java.util.Arrays;
import java.util.Optional;

public enum StateBooking {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static Optional<StateBooking> from(String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(state))
                .findFirst();
    }

    @Deprecated
    public static StateBooking fromStringOrThrow(String state) {
        return from(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));
    }
}