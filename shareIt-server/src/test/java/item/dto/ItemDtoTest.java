package item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
public class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemDtoResponse> json;
    @Autowired
    private JacksonTester<ItemDtoRequest> jsonItemDtoRequest;

    @Test
    void testItemDtoRequest() throws IOException {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .id(1L)
                .name("doll")
                .description("for playing with a doll")
                .available(true)
                .build();
        JsonContent<ItemDtoRequest> result = jsonItemDtoRequest.write(itemDtoRequest);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo("doll");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("for playing with a doll");
    }

    @Test
    void testItemDtoResponse() throws IOException {
        ItemDtoResponse itemDtoResponse = ItemDtoResponse.builder()
                .id(1L)
                .name("doll")
                .description("for playing with a doll")
                .available(true)
                .build();
        JsonContent<ItemDtoResponse> result = json.write(itemDtoResponse);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo("doll");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("for playing with a doll");
    }
}
