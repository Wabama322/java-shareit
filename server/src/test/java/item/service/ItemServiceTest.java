package item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;

    private final User owner = User.builder()
            .id(1L)
            .name("Nasty")
            .email("Nasty@gmail.com")
            .build();
    private final Item item = Item.builder()
            .id(1L)
            .description("All needed thing")
            .name("1st Item")
            .available(true)
            .owner(owner)
            .build();

    @Test
    void addItemValidAddTest() {
        Long ownerId = 1L;
        ItemDtoRequest itemDto = new ItemDtoRequest(null,
                "1st Item",
                "All needed thing",
                true,
                null);
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        ItemDtoResponse result = itemService.addItem(ownerId, itemDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1st Item", result.getName());
        assertEquals("All needed thing", result.getDescription());
        assertTrue(result.getAvailable());
        assertNull(result.getRequestId());
    }

    @Test
    public void getAllItemsWithBlankTextShouldReturnEmptyListTest() {
        String text = "";
        Pageable page = PageRequest.of(0, 10);
        Page<Item> actualResult = itemRepository.searchAvailableItemsByNameOrDescription(text, page);

        assertNull(actualResult);
    }

    @Test
    void findItemByIdTest() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Item result = itemRepository.findById(1L).orElse(null);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1st Item", result.getName());
        assertEquals("All needed thing", result.getDescription());
        assertEquals(owner, result.getOwner());
        assertTrue(result.getAvailable());
        assertNull(result.getRequest());
    }

    @Test
    public void updateItemValidTest() {
        Long ownerId = 1L;
        Long itemId = 2L;
        Long itemRequestId = 3L;
        ItemDtoRequest itemDto = new ItemDtoRequest(1L,
                "test",
                "description",
                true,
                3L);
        ItemDtoRequest updateItemDto = new ItemDtoRequest(1L,
                "test_update",
                "description_update",
                true,
                3L);
        ItemRequest itemRequest = ItemRequest.builder()
                .id(3L)
                .description("description")
                .created(LocalDateTime.now())
                .build();

        User user = new User();
        user.setId(ownerId);
        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setOwner(user);
        oldItem.setName("old name");
        oldItem.setDescription("old description");
        oldItem.setAvailable(false);
        itemRequest.setRequester(user);
        oldItem.setRequest(itemRequest);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(3L)).thenReturn(Optional.of(ItemRequest.builder().build()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        ItemDtoResponse result = itemService.updateItem(ownerId, itemId, itemDto);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("test", result.getName());
        assertEquals("description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(3L, result.getRequestId());

        ItemDtoResponse resultUpdated = itemService.updateItem(ownerId, itemId, updateItemDto);

        assertNotNull(resultUpdated);
        assertEquals(itemId, result.getId());
        assertEquals("test_update", resultUpdated.getName());
        assertEquals("description_update", resultUpdated.getDescription());
        assertTrue(resultUpdated.getAvailable());
        assertEquals(3L, resultUpdated.getRequestId());
    }

    @Test
    public void getItemTest() {
        Long itemId = 1L;
        Long userId = 2L;
        User owner = new User(1L, "sah", "Alena");

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(new Item(itemId, "Doll",
                        "Barbie",
                        true,
                        owner,
                        null)));
        when(commentRepository.findByItemIn(anyList(),
                any(Sort.class))).thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemInAndStatus(anyList(),
                eq(Status.APPROVED), any(Sort.class))).thenReturn(Collections.emptyList());

        ItemForBookingDto result = itemService.getItemDto(userId, itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Doll", result.getName());
        assertEquals("Barbie", result.getDescription());
        assertTrue(result.getAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    public void updateInvalidOwnerIdThrowsExceptionTest() {
        Long ownerId = 1L;
        Long itemId = 2L;
        ItemDtoRequest itemDto = new ItemDtoRequest(1L,
                "test1",
                "description1",
                true,
                null);
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.updateItem(ownerId, itemId,
                itemDto));
        assertEquals("Вещь с ID 2 не зарегистрирован!", ex.getMessage());
    }

    @Test
    public void failAddItemInvalidParamsTest() {
        User owner = new User(1L, "test@gmail.com", "Tester");

        ItemDtoRequest newItem = new ItemDtoRequest(null,
                null,
                null,
                null,
                null);
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.addItem(owner.getId(), newItem));

        ItemDtoRequest newItemWithoutName = new ItemDtoRequest(null,
                null,
                null,
                true,
                null);

        assertThrows(NotFoundException.class, () -> itemService.addItem(owner.getId(), newItemWithoutName));
        assertNotNull(exception);

        ItemDtoRequest newItemWithoutDescription = new ItemDtoRequest(null,
                "testName",
                null,
                true,
                null);
        assertThrows(NotFoundException.class, () -> itemService.addItem(owner.getId(), newItemWithoutDescription));
    }

    @Test
    public void addItemWithoutOwnerIdTest() {
        ItemDtoRequest itemDto = new ItemDtoRequest(null,
                "Item1",
                "new item1",
                true,
                null);

        assertThrows(NotFoundException.class, () -> {
            itemService.addItem(9L, itemDto);
        });
    }

    @Test
    public void shouldMapToCommentDtoListTest() {
        User owner = new User(1L,
                "Alena",
                "Alena@gmail.com");
        Item item = new Item(1L,
                "Doll",
                "Barbie",
                true,
                owner,
                null);

        User author = new User(3L,
                "test@gmail.com",
                "Tester");
        Comment comment1 = new Comment(1L, "text1", item, author, LocalDateTime.now());
        Comment comment2 = new Comment(1L, "text2", item, author, LocalDateTime.now());
        List<Comment> commentList = List.of(comment1, comment2);
        List<CommentDtoResponse> commentDto = CommentMapper.toCommentDtoList(commentList);

        assertNotNull(commentDto);
        assertEquals(commentDto.get(0).getText(), comment1.getText());
        assertEquals(commentDto.get(1).getText(), comment2.getText());
    }

    @Test
    public void addBookingAndCommentTest() {
        User owner = new User(1L,
                "test@gmail.com",
                "Tester");
        Item item = Item.builder()
                .id(1L)
                .name("Doll")
                .description("Barbie")
                .owner(owner)
                .build();
        List<Comment> comments = List.of(
                new Comment(1L, "Coll", item, owner, LocalDateTime.now()),
                new Comment(2L, "Fine", item, owner, LocalDateTime.now())
        );
        List<Booking> bookings = List.of(
                new Booking(1L,
                        LocalDateTime.now().minusDays(2),
                        LocalDateTime.now().minusDays(1),
                        item,
                        owner,
                        Status.APPROVED),
                new Booking(2L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        item,
                        owner,
                        Status.APPROVED)
        );
        LocalDateTime now = LocalDateTime.now();

        ItemForBookingDto result = itemService.addBookingAndComment(item, 1L, comments, bookings, now);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertNull(result.getAvailable());
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertEquals(comments.size(), result.getComments().size());
        assertEquals(comments.get(0).getId(), result.getComments().get(0).getId());
        assertEquals(comments.get(1).getId(), result.getComments().get(1).getId());
    }

    @Test
    public void getItemInvalidThrowsExceptionTest() {
        Long itemId = 1L;
        Long userId = 2L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.getItemDto(itemId, userId));
        assertEquals("Вещь с ID 2 не зарегистрирован!", ex.getMessage());
    }

    @Test
    public void getItemsInvalidOwnerIdThrowsExceptionTest() {
        Long ownerId = -1L;
        int from = 0;
        int size = 10;

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.getAllItemsUser(ownerId, from,
                size));
        assertEquals("Пользователь с ID -1 не зарегистрирован!", ex.getMessage());
    }

    @Test
    public void getItemsInvalidValueThrowsExceptionTest() {
        Long ownerId = 1L;
        int from = -1;
        int size = 10;

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.getAllItemsUser(ownerId, from,
                size));
        assertEquals("Пользователь с ID 1 не зарегистрирован!", ex.getMessage());
    }

    @Test
    public void toItemDtoListTest() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item(1L,
                "Doll",
                "Barbie",
                true,
                null,
                null));
        itemList.add(new Item(2L,
                "Great Doll",
                "Blue",
                false,
                null,
                null));
        List<ItemDtoResponse> expectedResult = new ArrayList<>();
        expectedResult.add(new ItemDtoResponse(1L,
                "Doll",
                "Barbie",
                true,
                null));
        expectedResult.add(new ItemDtoResponse(2L,
                "Great Doll",
                "Blue",
                false,
                null));
        List<ItemDtoResponse> actualResult = itemList.stream()
                .map(ItemMapper::toItemDtoResponse).collect(Collectors.toList());

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void toItemDtoListEmptyListTest() {
        List<Item> itemList = Collections.emptyList();
        List<ItemForItemRequestResponseDto> expectedResult = Collections.emptyList();
        List<ItemForItemRequestResponseDto> actualResult = ItemMapper.toItemForItemRequestsResponseDto(itemList);

        assertNotNull(actualResult);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void searchItemsByTextWhenTextIsBlankTest() {
        List<ItemSearchOfTextDto> itemDtoList = itemService.getSearchOfText("", 0, 10);

        assertEquals(List.of(), itemDtoList);
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
    public void addCommentItemNotFoundThrowsExceptionTest() {
        Long authorId = 1L;
        Long itemId = 1L;
        CommentDtoRequest commentDto = new CommentDtoRequest();
        User user = new User();
        user.setId(authorId);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> {
            itemService.addComment(itemId, authorId, commentDto);
        });
        assertEquals("Вещь с ID 1 не зарегистрирован!", ex.getMessage());
    }

    @Test
    void updateItemIfNotValidOwnerTest() {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDtoRequest itemDto = new ItemDtoRequest(1L,
                "test_update",
                "description_update",
                true,
                3L);
        itemDto.setName("Бочка для капусты");
        assertThrows(NotFoundException.class, () -> {
            itemService.updateItem(itemId, userId, itemDto);
        }, "Вещь с ID 1 не зарегистрирован!");
    }
}
