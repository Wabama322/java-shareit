package booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.utill.Constants;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                        .content(objectMapper.writeValueAsString(bookingDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
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

        mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .header(Constants.USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError()) // Ожидаем 500
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());

        Mockito.verify(bookingClient, Mockito.never())
                .addBooking(ArgumentMatchers.anyLong(), ArgumentMatchers.any());
    }

    @SneakyThrows
    @Test
    void getBookingTest() throws Exception {
        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).getBooking(1, 1L);
    }

    @SneakyThrows
    @Test
    void updatedBookingTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/bookings/{bookingId}?approved={approved}", 1, true)
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).updateBooking(1L, 1L, true);
    }

    @SneakyThrows
    @Test
    void getAllBookingByOwnerTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/owner")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/owner?state=UNICE")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/owner?state=WAITING")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/owner?from=-1&size=0")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    @SneakyThrows
    @Test
    void getAllBookingByUserTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings?state=WAITING")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings?from=-1&size=0")
                        .header(Constants.USER_HEADER, 1))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    BookingDtoRequest getBookingDtoRequest(LocalDateTime start, LocalDateTime end) {
        return new BookingDtoRequest(
                start,
                end, 1L
        );
    }
}
