package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;

public enum StateBooking {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static StateBooking getStateFromText(String text) {
        for (StateBooking state : StateBooking.values()) {
            if (state.toString().equals(text)) {
                return state;
            }
        }
        throw new NotFoundException("Неизвестный статус");
    }

    public static StateBooking fromString(String state) {
        try {
            return StateBooking.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }

}
