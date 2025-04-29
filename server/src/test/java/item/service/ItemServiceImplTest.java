package item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.dto.*;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        owner = User.builder().id(1L).name("Owner").email("owner@example.com").build();
        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();
    }

    @Test
    void updateItem_ShouldKeepOriginalValuesWhenNewOnesAreBlank() {
        ItemDtoRequest updateDto = ItemDtoRequest.builder()
                .name(" ")
                .description("")
                .available(null)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDtoResponse result = itemService.updateItem(1L, 1L, updateDto);

        assertEquals("Item", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_WithNonOwner_ShouldThrowAccessDeniedException() {
        User otherUser = User.builder().id(2L).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class, () ->
                itemService.updateItem(2L, 1L, new ItemDtoRequest()));
    }

    @Test
    void getSearchOfText_WithBlankText_ShouldReturnEmptyList() {
        List<ItemSearchOfTextDto> result = itemService.getSearchOfText("", 0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSearchOfText_WithValidText_ShouldReturnItems() {
        Page<Item> page = new PageImpl<>(List.of(item));
        when(itemRepository.findByNameOrDescription(anyString(), any(Pageable.class))).thenReturn(page);

        List<ItemSearchOfTextDto> result = itemService.getSearchOfText("test", 0, 10);
        assertEquals(1, result.size());
        assertEquals("Item", result.get(0).getName());
    }

    @Test
    void addComment_WithoutValidBooking_ShouldThrowBadRequestException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(BadRequestException.class, () ->
                itemService.addComment(1L, 1L, new CommentDtoRequest("Test")));
    }

    @Test
    void addComment_WithValidData_ShouldReturnComment() {
        Comment comment = Comment.builder()
                .id(1L)
                .text("Test")
                .author(owner)
                .item(item)
                .created(now)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDtoResponse result = itemService.addComment(1L, 1L, new CommentDtoRequest("Test"));

        assertNotNull(result);
        assertEquals("Test", result.getText());
        assertEquals("Owner", result.getAuthorName());
    }

    @Test
    void addItem_WithRequest_ShouldSetRequest() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        ItemDtoRequest itemDto = ItemDtoRequest.builder().requestId(1L).build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ItemDtoResponse result = itemService.addItem(1L, itemDto);

        assertNotNull(result);
        verify(itemRequestRepository).findById(1L);
    }

    @Test
    void getAllItemsUser_ShouldReturnWithBookingsAndComments() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).owner(owner).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerIdOrderById(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));
        when(commentRepository.findByItemIn(eq(List.of(item)), any(Sort.class)))
                .thenReturn(List.of());
        when(bookingRepository.findByItemInAndStatus(eq(List.of(item)), eq(Status.APPROVED), any(Sort.class)))
                .thenReturn(List.of());

        List<ItemForBookingDto> result = itemService.getAllItemsUser(1L, 0, 10);

        assertEquals(1, result.size());
        verify(userRepository).findById(1L);
    }

    @Test
    void getItemDto_ShouldReturnWithBookingsAndComments() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).owner(owner).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIn(eq(List.of(item)), any(Sort.class)))
                .thenReturn(List.of());
        when(bookingRepository.findByItemInAndStatus(eq(List.of(item)), eq(Status.APPROVED), any(Sort.class)))
                .thenReturn(List.of());

        ItemForBookingDto result = itemService.getItemDto(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}
