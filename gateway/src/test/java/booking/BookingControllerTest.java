package booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.utill.Constants;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = ShareItGateway.class)
public class BookingControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    BookingClient bookingClient;
    @MockBean
    UserRepository userRepository;

    @SneakyThrows
    @Test
    void addBookingTest() throws Exception {
        BookingDtoRequest bookingDtoRequest = getBookingDtoRequest(LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(10));
        String bookingJson = objectMapper.writeValueAsString(bookingDtoRequest);
        ResponseEntity<Object> response = new ResponseEntity<>(bookingJson, HttpStatus.OK);
        when(bookingClient.addBooking(ArgumentMatchers.anyLong(), ArgumentMatchers.any()))
                .thenReturn(response);
        String content = mockMvc.perform(post("/bookings")
                        .header(Constants.USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(bookingJson, content);
    }

    @SneakyThrows
    @Test
    void addBookingWrongTest() throws Exception {
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

    @SneakyThrows
    @Test
    void getBookingTest() throws Exception {
        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());
        verify(bookingClient).getBooking(1L, 1L);
    }

    @SneakyThrows
    @Test
    void approveBookingTest() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}?approved={approved}", 1, true)
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());
        verify(bookingClient).approveBooking(1L, 1L, true);
    }

    @SneakyThrows
    @Test
    void getAllBookingByOwner_InvalidState_Returns400() throws Exception {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        mockMvc.perform(get("/bookings/owner")
                        .header(Constants.USER_HEADER, 1L)
                        .param("state", "UNICE"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Unknown state")));
    }

    @SneakyThrows
    @Test
    void getAllBookingByUserTest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings?state=WAITING")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings?from=-1&size=0")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    BookingDtoRequest getBookingDtoRequest(LocalDateTime start, LocalDateTime end) {
        return new BookingDtoRequest(
                start,
                end, 1L
        );
    }
}
