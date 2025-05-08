package booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BookingTest {
    User user = User.builder()
            .id(1L)
            .name("userName1")
            .email("test@mail.fg")
            .build();
    Item item = Item.builder()
            .id(1L)
            .name("item1")
            .description("item 1 Oh")
            .available(false)
            .owner(user)
            .build();
    Booking booking = Booking.builder()
            .id(1L)
            .item(item)
            .booker(user)
            .status(Status.WAITING)
            .start(null)
            .end(null)
            .build();
    Booking booking2 = Booking.builder()
            .id(1L)
            .item(item)
            .booker(user)
            .status(Status.WAITING)
            .start(null)
            .end(null)
            .build();
    Booking booking3 = Booking.builder()
            .id(1L)
            .item(null)
            .booker(null)
            .status(Status.APPROVED)
            .start(null)
            .end(null)
            .build();

    @Test
    void testBookingHashCode() {
        assertEquals(booking, booking2);
        assertNotEquals(booking, booking3);
    }
}
