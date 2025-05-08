package request.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ItemRequestTest {
    User user = User.builder()
            .id(1L)
            .name("userName1")
            .email("test@mail.ru")
            .build();
    Item item = Item.builder()
            .name("item1")
            .description("item 1")
            .available(true)
            .owner(user)
            .build();
    ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .description("doll")
            .requester(user)
            .created(null)
            .build();
    ItemRequest itemRequest2 = ItemRequest.builder()
            .id(1L)
            .description("doll")
            .requester(user)
            .created(null)
            .build();
    ItemRequest itemRequest3 = ItemRequest.builder()
            .id(1L)
            .description("Blue")
            .requester(user)
            .created(LocalDateTime.now())
            .build();

    @Test
    void itemRequestHashCodeTest() {
        assertEquals(itemRequest, itemRequest2);
        assertNotEquals(itemRequest, itemRequest3);
    }
}
