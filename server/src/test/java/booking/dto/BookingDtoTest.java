package booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingForResponse;
import ru.practicum.shareit.booking.dto.UserWithIdAndNameDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
public class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingForResponse> json;
    @Autowired
    private JacksonTester<BookingDtoRequest> jsonBookingDtoRequest;

    @Test
    void testBookingForResponse() throws IOException {
        ItemWithBookingDto itemDto = ItemWithBookingDto.builder()
                .id(1L)
                .name("Ilay")
                .build();

        UserWithIdAndNameDto userDto = UserWithIdAndNameDto.builder()
                .id(1L)
                .name("Alena")
                .build();

        BookingForResponse bookingDto = BookingForResponse.builder()
                .id(1L)
                .item(itemDto)
                .booker(userDto)
                .status(Status.WAITING)
                .build();

        JsonContent<BookingForResponse> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Ilay");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Alena");
    }

    @Test
    void testBookingDtoRequest() throws IOException {

        ItemWithBookingDto itemDto = ItemWithBookingDto.builder()
                .id(1L)
                .name("Ilay")
                .build();

        BookingDtoRequest bookingDto = BookingDtoRequest.builder()
                .itemId(itemDto.getId())
                .build();
        JsonContent<BookingDtoRequest> result = jsonBookingDtoRequest.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(1);
    }
}
