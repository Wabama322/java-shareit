package request.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestDtoMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserWithIdDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ItemRequestMapperTest {
    User user;
    UserWithIdDto withIdDto;
    User owner;
    UserWithIdDto withIdDto1;
    Item item;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .name("name")
                .email("mail@gmail.com")
                .build();

        withIdDto = UserMapper.toUserWithIdDtoMapper(user);

        owner = User.builder()
                .id(2L)
                .name("name owner 2")
                .email("owner@gmail.com")
                .build();

        withIdDto1 = UserMapper.toUserWithIdDtoMapper(owner);

        item = Item.builder()
                .id(1L)
                .name("name item 1")
                .description("desc item 1")
                .owner(owner)
                .available(true)
                .build();
    }

    @Test
    void toItemRequestResponseDtoTest() {
        var original = new ItemRequest();
        original.setId(1L);
        original.setRequester(user);
        original.setItems(List.of(item));
        original.setDescription("Description");
        original.setCreated(LocalDateTime.now());
        var result = ItemRequestDtoMapper.toItemRequestResponseDto(original);

        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(original.getCreated(), result.getCreated());
        assertNotNull(result.getItems());
        assertNotNull(result.getRequester());
    }

    @Test
    void toNewItemRequestTest() {
        var original = ItemRequestDto.builder()
                .description("desc item 1")
                .build();

        var result = ItemRequestDtoMapper.toItemRequest(original, user);

        assertNotNull(result);
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(user, result.getRequester());
    }

    @Test
    void toItemRequestsResponseDtoTest() {
        var original = new ItemRequest();
        original.setId(1L);
        original.setRequester(user);
        original.setItems(List.of(item));
        original.setDescription("Description");
        original.setCreated(LocalDateTime.now());
        var itemRequests = new ArrayList<ItemRequest>();
        itemRequests.add(original);
        var result = ItemRequestDtoMapper.toItemRequestsResponseDto(itemRequests);

        assertNotNull(result);
        assertEquals(itemRequests.get(0).getId(), result.get(0).getId());
        assertEquals(itemRequests.get(0).getDescription(), result.get(0).getDescription());
        assertEquals(itemRequests.get(0).getCreated(), result.get(0).getCreated());
        assertNotNull(result.get(0).getItems());
        assertNotNull(result.get(0).getRequester());
    }
}
