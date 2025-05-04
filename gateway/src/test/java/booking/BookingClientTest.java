package booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    private final String serverUrl = "http://localhost:8080";
    private final Long userId = 1L;
    private final Long bookingId = 1L;
    private final Integer from = 0;
    private final Integer size = 10;
    private final BookingState state = BookingState.ALL;
    private final BookingDtoRequest bookingDtoRequest = new BookingDtoRequest();

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);

        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        bookingClient = new BookingClient(serverUrl, builder);
    }

    @Test
    void getAllBookingsByUser_ShouldCallGetWithCorrectParameters() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Object> response = bookingClient.getAllBookingsByUser(userId, state, from, size);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addBooking_ShouldCallPostWithCorrectParameters() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Object> response = bookingClient.addBooking(userId, bookingDtoRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getBooking_ShouldCallGetWithCorrectPath() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Object> response = bookingClient.getBooking(userId, bookingId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void approveBooking_ShouldCallPatchWithCorrectParameters() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class),
                anyMap()
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Object> response = bookingClient.approveBooking(userId, bookingId, true);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllBookingsByOwner_ShouldReturnErrorResponseWhenExceptionThrown() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                anyMap()
        )).thenThrow(exception);

        ResponseEntity<Object> response = bookingClient.getAllBookingsByOwner(userId, state, from, size);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAllBookingsByOwner_ShouldReturnInternalServerErrorOnUnexpectedException() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                anyMap()
        )).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Object> response = bookingClient.getAllBookingsByOwner(userId, state, from, size);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("Internal server error"));
    }
}