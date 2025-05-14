package item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingLastAndNextDto;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


public class ItemMapperTest {
    @Test
    void toItemDtoResponseTest() {
        var original = new Item();
        original.setId(1L);
        original.setDescription("Description");
        original.setName("Alena");
        original.setRequest(null);
        original.setAvailable(true);
        var result = ItemMapper.toItemDto(original);

        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(original.getAvailable(), result.getAvailable());
        assertNull(result.getRequestId());
    }

    @Test
    void toNewItemTest() {
        var original = new ItemDtoRequest();
        original.setId(1L);
        original.setName("Alena");
        original.setDescription("Description");
        original.setAvailable(true);
        var result = ItemMapper.toItem(original);

        assertNotNull(result);
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(original.getAvailable(), result.getAvailable());
        assertNull(result.getRequest());
        assertNull(result.getOwner());
    }

    @Test
    void toItemForBookingDtoTest() {
        var lastBooking = new BookingLastAndNextDto();
        lastBooking.setId(1L);
        lastBooking.setBookerId(1L);
        var nextBooking = new BookingLastAndNextDto();
        nextBooking.setId(2L);
        nextBooking.setBookerId(1L);
        var comments = new ArrayList<CommentDtoResponse>();
        var comment = new CommentDtoResponse();
        comment.setId(1L);
        comment.setText("Cool");
        comment.setAuthorName("Alena");
        comment.setCreated(LocalDateTime.now());
        comments.add(comment);
        var original = new Item();
        original.setId(1L);
        original.setDescription("Description");
        original.setName("Alena");
        original.setRequest(null);
        original.setAvailable(true);
        var result = ItemMapper.toItemWithBookings(original, lastBooking, nextBooking, comments);

        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(original.getAvailable(), result.getAvailable());
        assertEquals(lastBooking, result.getLastBooking());
        assertEquals(nextBooking, result.getNextBooking());
        assertEquals(comments, result.getComments());
    }

    @Test
    void toItemForItemRequestResponseDtoTest() {
        var original = new Item();
        original.setId(1L);
        original.setDescription("Description");
        original.setName("Alena");
        original.setRequest(null);
        original.setAvailable(true);
        var result = ItemMapper.toRequestItemDto(original);

        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(original.getAvailable(), result.getAvailable());
        assertNull(result.getRequestId());
    }

    @Test
    void toItemForItemRequestsResponseDtoTest() {
        var original = new Item();
        original.setId(1L);
        original.setDescription("Description");
        original.setName("Alena");
        original.setRequest(null);
        original.setAvailable(true);
        var items = new ArrayList<Item>();
        items.add(original);
        var result = ItemMapper.toRequestItemDtoList(items);

        assertNotNull(result);
        assertEquals(items.get(0).getId(), result.get(0).getId());
        assertEquals(items.get(0).getName(), result.get(0).getName());
        assertEquals(items.get(0).getDescription(), result.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), result.get(0).getAvailable());
        assertNull(result.get(0).getRequestId());
    }
}
