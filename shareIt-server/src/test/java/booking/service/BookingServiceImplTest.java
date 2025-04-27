package booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingForResponse;
import ru.practicum.shareit.booking.model.Booking;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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

        assertEquals("Нельзя бронировать свою вещь", exception.getMessage());
    }

    @Test
    void addBooking_WithUnavailableItem_ShouldThrowBadRequestException() {
        item.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(booker.getId(), bookingDtoRequest));

        assertEquals("Вещь недоступна", exception.getMessage());
    }

    @Test
    void addBooking_WithInvalidDates_ShouldThrowBadRequestException() {
        bookingDtoRequest.setEnd(LocalDateTime.now().minusDays(1));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        Exception exception = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(booker.getId(), bookingDtoRequest));

        assertEquals("Некорректные даты бронирования", exception.getMessage());
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

        assertEquals("Пользователь не является владельцем вещи ", exception.getMessage());
    }

    @Test
    void updateBooking_AlreadyApproved_ShouldThrowBadRequestException() {
        booking.setStatus(APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(BadRequestException.class,
                () -> bookingService.updateBooking(1L, owner.getId(), true));

        assertEquals("Данное бронирование уже внесено и имеет статус APPROVED", exception.getMessage());
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

        assertEquals("Бронирование не найдено или доступ запрещен", exception.getMessage());
    }

    @Test
    void getAllBookingByUser_ShouldReturnBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingForResponse> result = bookingService.getAllBookingByUser("ALL", booker.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllBookingByOwner_ShouldReturnBookings() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingForResponse> result = bookingService.getAllBookingByOwner("ALL", owner.getId(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllBookingByOwner_WithInvalidPagination_ShouldThrowBadRequestException() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> bookingService.getAllBookingByOwner("ALL", owner.getId(), -1, 0));
    }
}
