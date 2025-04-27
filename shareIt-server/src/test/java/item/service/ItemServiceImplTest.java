package item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ContextConfiguration(classes = ShareItServer.class)
class ItemServiceImplTest {
    LocalDateTime now = LocalDateTime.now();
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void getItemWithBookingAndCommentTest() {
        CommentDtoRequest inputCommentDto = CommentDtoRequest.builder()
                .text("new comment for test")
                .build();

        User owner2 = User.builder()
                .id(2L)
                .name("name for owner")
                .email("owner2@gmail.com")
                .build();

        User userForTest2 = User.builder()
                .id(1L)
                .name("name user for test 2")
                .email("userForTest2@gmail.com")
                .build();

        Item banana = Item.builder()
                .id(1L)
                .name("banana")
                .description("item Banana")
                .owner(owner2).build();

        Booking bookingFromBd = Booking.builder()
                .id(1L)
                .item(banana)
                .booker(userForTest2)
                .start(now.minusDays(10))
                .end(now.minusDays(5))
                .build();

        Item itemFromBd = Item.builder()
                .id(1L)
                .name("name for item")
                .description("for item")
                .owner(owner2)
                .available(true)
                .build();

        CommentDtoResponse commentDto = CommentDtoResponse.builder()
                .id(1L)
                .text("comment 1")
                .authorName("name user for test 2")
                .created(now.minusDays(5))
                .build();

        Comment outputComment = Comment.builder()
                .id(1L)
                .author(userForTest2)
                .text("comment 1")
                .item(itemFromBd)
                .build();

        UserRepository userRepositoryJpa2 = mock(UserRepository.class);
        ItemRepository itemRepositoryJpa2 = mock(ItemRepository.class);
        CommentRepository commentRepository2 = mock(CommentRepository.class);

        when(userRepositoryJpa2.findById(any())).thenReturn(Optional.of(userForTest2));
        when(itemRepositoryJpa2.findById(any())).thenReturn(Optional.of(itemFromBd));
        when(commentRepository2.save(any())).thenReturn(outputComment);
    }

    @Test
    public void addItemTest() {
        UserDto userDto = new UserDto(1L,
                "name",
                "mail@gmail.com"
        );
        userService.createUser(userDto);

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("mail@gmail.com")
                .build();

        UserDto userDtoResponse = UserMapper.toUserDtoResponse(user);

        ItemDtoRequest itemDtoRequest1 = ItemDtoRequest.builder()
                .id(1L)
                .name("name for item 1")
                .description("description for item 1")
                .available(true)
                .build();
        Item item1 = Item.builder()
                .id(1L)
                .name("name for item 1")
                .description("description for item 1")
                .owner(user)
                .available(true)
                .build();
        ItemDtoResponse itemDtoResponse = ItemMapper.toItemDtoResponse(item1);

        assertNotNull(itemDtoResponse);
        assertEquals(itemDtoResponse.getId(), 1L);
        assertEquals(itemDtoResponse.getDescription(), itemDtoRequest1.getDescription());
        assertEquals(itemDtoResponse.getName(), itemDtoRequest1.getName());
        assertEquals(itemDtoResponse.getAvailable(), itemDtoRequest1.getAvailable());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.updateItem(user.getId(),
                itemDtoRequest1.getId(), itemDtoRequest1));
        assertEquals("Вещь с ID 1 не зарегистрирован!", ex.getMessage());
    }

    @Test
    public void addCommentAuthorNullThrowExceptionTest() {
        Long authorId = 5L;
        Long itemId = 3L;
        CommentDtoRequest commentDto = new CommentDtoRequest("Test comment");

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.addComment(itemId, authorId,
                commentDto));
        assertEquals("Вещь с ID 3 не зарегистрирован!", ex.getMessage());
    }

    @Test
    public void getAllItemsUserTest() {
        Long ownerId = 1L;
        int from = -1;
        int size = 10;

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.getAllItemsUser(ownerId, from,
                size));
        assertEquals("Пользователь с ID 1 не зарегистрирован!", ex.getMessage());
    }
}
