package request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.ItemForItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.dto.UserForItemRequestDto;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
public class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    @Test
    void itemDtoTest() throws IOException {
        ItemForItemRequestResponseDto itemDto = ItemForItemRequestResponseDto.builder()
                .id(1L)
                .name("Doll")
                .description("Barbie")
                .available(true)
                .build();

        UserForItemRequestDto userDto = UserForItemRequestDto.builder()
                .id(1L)
                .name("Alena")
                .build();

        ItemRequestResponseDto requestResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Some items for fun")
                .requester(userDto)
                .build();

        itemDto.setRequestId(requestResponseDto.getId());
        requestResponseDto.setItems(List.of(itemDto));
        JsonContent<ItemRequestResponseDto> result = json.write(requestResponseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Some items for fun");
        assertThat(result).extractingJsonPathNumberValue("$.requester.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Doll");
        assertThat(result).extractingJsonPathStringValue("$.items[0].description")
                .isEqualTo("Barbie");
    }
}
