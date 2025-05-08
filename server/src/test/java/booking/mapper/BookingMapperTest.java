package booking.mapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDtoRequest;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserWithIdDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BookingMapperTest {
    User user;
    UserWithIdDto userForResponse;
    User owner;
    UserWithIdDto ownerForResponseDto;
    Item item;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("name")
                .email("mail@gmail.com")
                .build();

        userForResponse = UserMapper.toUserWithIdDtoMapper(user);

        owner = User.builder()
                .id(2L)
                .name("name owner 2")
                .email("owner@jjgv.zw")
                .build();

        ownerForResponseDto = UserMapper.toUserWithIdDtoMapper(owner);

        ItemDtoRequest itemDtoRequest1 = ItemDtoRequest.builder().id(1L)
                .name("name item 1").description("desc item 1").available(true).build();

        item = Item.builder()
                .id(1L)
                .name(itemDtoRequest1.getName())
                .description(itemDtoRequest1.getDescription())
                .owner(owner)
                .available(true)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void toNewBookingTest() {
        var original = new BookingDtoRequest();
        original.setItemId(1L);
        original.setStart(LocalDateTime.now().minusDays(1));
        original.setEnd(LocalDateTime.now());
        var result = BookingMapper.toBooking(original, item, user);

        assertNotNull(result);
        assertEquals(original.getItemId(), result.getItem().getId());
        assertEquals(original.getStart(), result.getStart());
        assertEquals(original.getEnd(), result.getEnd());
        assertEquals(user, result.getBooker());
        assertNull(result.getStatus());
    }

    @Test
    void toItemBookingInfoDtoTest() {
        var original = new Booking();
        original.setId(1L);
        original.setBooker(user);
        original.setStart(LocalDateTime.now().minusDays(1));
        original.setEnd(LocalDateTime.now());
        var result = BookingMapper.toItemBookingInfoDto(original);

        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getBooker().getId(), result.getBookerId());
        assertEquals(original.getStart(), result.getStart());
        assertEquals(original.getEnd(), result.getEnd());
    }

    @Test
    void toBookingForResponseMapperTest() {
        var original = new Booking();
        original.setId(1L);
        original.setBooker(user);
        original.setItem(item);
        original.setStatus(Status.WAITING);
        original.setStart(LocalDateTime.now().minusDays(1));
        original.setEnd(LocalDateTime.now());
        var result = BookingMapper.toBookingForResponseMapper(original);

        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getBooker().getId(), result.getBooker().getId());
        assertEquals(original.getStart(), result.getStart());
        assertEquals(original.getEnd(), result.getEnd());
        assertEquals(original.getItem().getId(), result.getItem().getId());
        assertEquals(original.getItem().getName(), result.getItem().getName());
        assertEquals(original.getStatus(), result.getStatus());
    }
}
