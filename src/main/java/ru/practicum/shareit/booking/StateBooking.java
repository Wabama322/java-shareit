package ru.practicum.shareit.booking;

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
}
