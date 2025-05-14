package booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingForResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.model.Status.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDtoRequest bookingDtoRequest;
    private BookingForResponse bookingForResponse;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(WAITING);

        bookingDtoRequest = new BookingDtoRequest();
        bookingDtoRequest.setItemId(1L);
        bookingDtoRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingDtoRequest.setEnd(LocalDateTime.now().plusDays(2));

        bookingForResponse = new BookingForResponse();
        bookingForResponse.setId(1L);
        bookingForResponse.setStart(booking.getStart());
        bookingForResponse.setEnd(booking.getEnd());
        bookingForResponse.setStatus(WAITING);
    }

    @Test
    void addBooking_ShouldCreateBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            savedBooking.setId(1L);
            return savedBooking;
        });

        BookingForResponse result = bookingService.addBooking(booker.getId(), bookingDtoRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        assertEquals(bookingDtoRequest.getStart(), result.getStart());
        assertEquals(bookingDtoRequest.getEnd(), result.getEnd());
        assertEquals(WAITING, result.getStatus());
    }

    @Test
    void addBooking_WithOwnItem_ShouldThrowBadRequestException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(owner.getId(), bookingDtoRequest));

        assertEquals("Владелец не может бронировать свою вещь", exception.getMessage());
    }

    @Test
    void addBooking_WithUnavailableItem_ShouldThrowBadRequestException() {
        item.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(booker.getId(), bookingDtoRequest));

        assertEquals("Вещь недоступна для бронирования", exception.getMessage());
    }

    @Test
    void addBooking_WithInvalidDates_ShouldThrowBadRequestException() {
        bookingDtoRequest.setEnd(LocalDateTime.now().minusDays(1));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(booker.getId(), bookingDtoRequest));

        assertEquals("Дата начала должна быть раньше даты окончания", exception.getMessage());
    }

    @Test
    void updateBooking_ShouldApproveBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingForResponse result = bookingService.updateBooking(1L, owner.getId(), true);

        assertNotNull(result);
        assertEquals(APPROVED, result.getStatus());
    }

    @Test
    void updateBooking_WithWrongOwner_ShouldThrowAccessDeniedException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.updateBooking(1L, 999L, true));

        assertEquals("Только владелец может подтверждать бронирование", exception.getMessage());
    }

    @Test
    void updateBooking_AlreadyApproved_ShouldThrowBadRequestException() {
        booking.setStatus(APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(BadRequestException.class,
                () -> bookingService.updateBooking(1L, owner.getId(), true));

        assertEquals("Статус уже изменен", exception.getMessage());
    }

    @Test
    void getBooking_ShouldReturnBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingForResponse result = bookingService.getBooking(1L, booker.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBooking_WithUnauthorizedUser_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(1L, 999L));

        assertEquals("Доступ запрещен", exception.getMessage());
    }

    @Test
    void getAllBookingByUser_ShouldReturnBookings() {
        when(userRepository.existsById(booker.getId())).thenReturn(true);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingForResponse> result = bookingService.getAllBookingByUser("ALL", booker.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllBookingByOwner_ShouldReturnBookings() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingForResponse> result = bookingService.getAllBookingByOwner("ALL", owner.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any(Pageable.class));
    }

    @Test
    void getAllBookingByOwner_WithInvalidPagination_ShouldThrowBadRequestException() {
        when(userRepository.existsById(anyLong())).thenReturn(true); // Пользователь существует

        assertThrows(BadRequestException.class,
                () -> bookingService.getAllBookingByOwner("ALL", owner.getId(), -1, 0));
    }

    @Test
    void addBooking_WhenItemAlreadyBooked_ShouldThrowRuntimeException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdAndStartLessThanEqualAndEndGreaterThanEqual(
                eq(item.getId()), any(), any(), any()))
                .thenReturn(List.of(new Booking()));

        assertThrows(RuntimeException.class,
                () -> bookingService.addBooking(booker.getId(), bookingDtoRequest));
    }

    @Test
    void updateBooking_WhenRejected_ShouldReturnRejectedStatus() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingForResponse result = bookingService.updateBooking(1L, owner.getId(), false);

        assertNotNull(result);
        assertEquals(REJECTED, result.getStatus());
    }

    @Test
    void addBooking_WhenEndBeforeStart_ShouldThrowBadRequestException() {
        BookingDtoRequest invalidRequest = new BookingDtoRequest();
        invalidRequest.setStart(LocalDateTime.now().plusDays(2));
        invalidRequest.setEnd(LocalDateTime.now().plusDays(1));
        invalidRequest.setItemId(1L);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(booker.getId(), invalidRequest));

        assertEquals("Дата начала должна быть раньше даты окончания", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"})
    void getAllBookingByUser_ShouldCallRepositoryMethod(String state) {
        when(userRepository.existsById(anyLong())).thenReturn(true); // Добавляем мок

        if ("ALL".equals(state)) {
            when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any()))
                    .thenReturn(List.of(booking));
        }

        assertDoesNotThrow(() ->
                bookingService.getAllBookingByUser(state, booker.getId(), 0, 10));
    }

    private static Stream<Arguments> provideStateTestCases() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of("ALL", now, 1),
                Arguments.of("CURRENT", now, 1),
                Arguments.of("PAST", now, 1),
                Arguments.of("FUTURE", now, 1),
                Arguments.of("WAITING", now, 1),
                Arguments.of("REJECTED", now, 1)
        );
    }

    @Test
    void getAllBookingByOwner_WithPagination_ShouldUseCorrectPageable() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any()))
                .thenReturn(List.of(booking, booking));

        List<BookingForResponse> result = bookingService.getAllBookingByOwner("ALL", owner.getId(), 0, 2);

        assertEquals(2, result.size());
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(eq(owner.getId()),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 2));
    }

    @Test
    void getAllBookingByUser_WithInvalidState_ShouldThrowBadRequestException() {
        when(userRepository.existsById(anyLong())).thenReturn(true); // Добавляем мок

        assertThrows(BadRequestException.class,
                () -> bookingService.getAllBookingByUser("INVALID_STATE", 1L, 0, 10));
    }

    @Test
    void getAllBookingByOwner_WithNoUser_ShouldThrow() {
        long invalidUserId = 999L;
        when(userRepository.existsById(invalidUserId)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllBookingByOwner("ALL", invalidUserId, 0, 10));
    }

    @Test
    void getAllBookingByUser_WithFutureState_ShouldReturnFutureBookings() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        User booker = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).name("Item").build();

        Booking futureBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                anyLong(),
                any(LocalDateTime.class),
                any(Pageable.class))
        ).thenReturn(List.of(futureBooking));

        List<BookingForResponse> result = bookingService.getAllBookingByUser("FUTURE", 1L, 0, 10);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Item", result.get(0).getItem().getName());
    }

    @Test
    void updateBooking_WhenAlreadyApproved_ShouldThrowBadRequestException() {
        Booking booking = Booking.builder()
                .status(Status.APPROVED)
                .item(Item.builder()
                        .owner(User.builder().id(1L).build())
                        .build())
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class,
                () -> bookingService.updateBooking(1L, 1L, true));
    }
}