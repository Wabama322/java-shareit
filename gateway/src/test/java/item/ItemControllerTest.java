package item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.utill.Constants;

@AutoConfigureMockMvc
@SpringBootTest(classes = ShareItGateway.class)
public class ItemControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemClient itemClient;

    @Test
    void testAddItemWrong() throws Exception {
        int userId = 1;
        ItemDtoRequest itemDto = getItemDto("");

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header(Constants.USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());

        Mockito.verify(itemClient, Mockito.never())
                .postItem(ArgumentMatchers.any(), ArgumentMatchers.anyLong());
    }

    @Test
    void getItem() throws Exception {
        long itemId = 1L;
        long userId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.get("/items/{itemId}", itemId)
                        .header(Constants.USER_HEADER, userId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemClient).getItem(itemId, userId);
    }

    @Test
    void testItemExceptionStatus500() throws Exception {
        long itemId = 69;
        long userId = 1;
        mockMvc.perform(MockMvcRequestBuilders.get("/items/{itemId}", itemId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());

        Mockito.verify(itemClient, Mockito.never()).getItem(itemId, userId);
    }

    @Test
    void testItemsByOwner() throws Exception {
        long userId = 1;
        int from = 1;
        int size = 10;
        mockMvc.perform(MockMvcRequestBuilders.get("/items?from={from}&size={size}", from, size)
                        .header(Constants.USER_HEADER, userId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemClient).getAllItemsUser(userId, 1, 10);
    }

    @Test
    public void testSearchItemsByTextNullSizeTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .param("text", "one item")
                        .param("from", "1")
                        .param("size", "0")
                        .header(Constants.USER_HEADER, "1"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    @Test
    void testItemsByOwnerWrongPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .param("from", "-1")
                        .param("size", "-10")
                        .header(Constants.USER_HEADER, "1"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());

        Mockito.verify(itemClient, Mockito.never())
                .getAllItemsUser(ArgumentMatchers.anyLong(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
    }

    ItemDtoRequest getItemDto(String name) {
        return new ItemDtoRequest(
                1L,
                name,
                "Описание",
                false,
                null
        );
    }
}
