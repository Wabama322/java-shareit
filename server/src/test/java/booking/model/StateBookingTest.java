package booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.StateBooking;
import ru.practicum.shareit.exception.BadRequestException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StateBookingTest {

    @Test
    void from_ShouldReturnCorrectStateForValidInput() {
        assertEquals(Optional.of(StateBooking.ALL), StateBooking.from("ALL"));
        assertEquals(Optional.of(StateBooking.PAST), StateBooking.from("PAST"));
        assertEquals(Optional.of(StateBooking.CURRENT), StateBooking.from("CURRENT"));
        assertEquals(Optional.of(StateBooking.FUTURE), StateBooking.from("FUTURE"));
        assertEquals(Optional.of(StateBooking.REJECTED), StateBooking.from("REJECTED"));
        assertEquals(Optional.of(StateBooking.WAITING), StateBooking.from("WAITING"));
    }

    @Test
    void from_ShouldBeCaseInsensitive() {
        assertEquals(Optional.of(StateBooking.ALL), StateBooking.from("all"));
        assertEquals(Optional.of(StateBooking.WAITING), StateBooking.from("waiting"));
    }

    @Test
    void from_ShouldReturnEmptyForInvalidState() {
        assertEquals(Optional.empty(), StateBooking.from("INVALID_STATE"));
        assertEquals(Optional.empty(), StateBooking.from(""));
        assertEquals(Optional.empty(), StateBooking.from(null));
    }

    @Test
    void fromStringOrThrow_ShouldThrowForInvalidState() {
        assertThrows(BadRequestException.class, () -> StateBooking.fromStringOrThrow("INVALID_STATE"));
    }

    @Test
    void fromStringOrThrow_ShouldReturnStateForValidInput() {
        assertEquals(StateBooking.ALL, StateBooking.fromStringOrThrow("ALL"));
        assertEquals(StateBooking.WAITING, StateBooking.fromStringOrThrow("WAITING"));
    }
}
