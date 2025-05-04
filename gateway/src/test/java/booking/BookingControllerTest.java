package booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.utill.Constants;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(classes = ShareItGateway.class)
public class BookingControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @MockBean
    private UserClient userClient;

    @Test
    @SneakyThrows
    void addBookingTest() {
        BookingDtoRequest bookingDtoRequest = getBookingDtoRequest(
                LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(10)
        );

        when(userClient.getUserById(anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        when(bookingClient.addBooking(anyLong(), any()))
                .thenReturn(ResponseEntity.ok(bookingDtoRequest));

        mockMvc.perform(post("/bookings")
                        .header(Constants.USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingDtoRequest)));

        verify(bookingClient).addBooking(1L, bookingDtoRequest);
    }

    @Test
    @SneakyThrows
    void addBookingWithInvalidDatesTest() {
        BookingDtoRequest bookingDtoRequest = getBookingDtoRequest(
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(3)
        );

        mockMvc.perform(post("/bookings")
                        .header(Constants.USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(bookingClient, never()).addBooking(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void getBookingTest() {
        when(userClient.getUserById(anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookingClient).getBooking(1L, 1L);
    }

    @Test
    @SneakyThrows
    void approveBookingTest() {
        when(userClient.getUserById(anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header(Constants.USER_HEADER, 1)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(bookingClient).approveBooking(1L, 1L, true);
    }

    @Test
    @SneakyThrows
    void getAllBookingByOwnerWithInvalidStateTest() {
        when(userClient.getUserById(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header(Constants.USER_HEADER, 1L)
                        .param("state", "UNKNOWN"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Unknown state")));
    }

    @Test
    @SneakyThrows
    void getAllBookingByUserTest() {
        when(userClient.getUserById(anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        when(bookingClient.getAllBookingsByUser(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header(Constants.USER_HEADER, 1)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header(Constants.USER_HEADER, 1)
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @SneakyThrows
    void getAllBookingByOwnerWhenUserNotFoundTest() {
        when(userClient.getUserById(anyLong()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(get("/bookings/owner")
                        .header(Constants.USER_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    private BookingDtoRequest getBookingDtoRequest(LocalDateTime start, LocalDateTime end) {
        return new BookingDtoRequest(start, end, 1L);
    }
}
