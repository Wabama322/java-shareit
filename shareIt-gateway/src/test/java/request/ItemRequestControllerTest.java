package request;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.utill.Constants;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = ShareItGateway.class)
public class ItemRequestControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemRequestClient itemRequestClient;

    @Test
    void addItemRequestTest() throws Exception {
        ItemRequestDto itemRequestDto = getItemRequestDto();
        String itemRequestJson = objectMapper.writeValueAsString(itemRequestDto);
        ResponseEntity<Object> response = new ResponseEntity<>(itemRequestJson, HttpStatus.OK);
        when(itemRequestClient.addItemRequest(anyLong(), any())).thenReturn(response);
        String content = mockMvc.perform(post("/requests")
                        .header(Constants.USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(objectMapper.writeValueAsString(itemRequestDto), content);
    }

    @Test
    void getItemRequestsByUserIdTest() throws Exception {
        mockMvc.perform(get("/requests")
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(itemRequestClient).getItemRequestsByUserId(1L);
    }

    @Test
    void getItemRequestTest() throws Exception {
        mockMvc.perform(get("/requests/{requestId}", 1)
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(itemRequestClient).getItemRequest(1L, 1L);
    }

    @Test
    void getAllItemRequestsTest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(itemRequestClient).getAllItemRequests(1L, 0, 20);
    }

    @Test
    void getAllItemRequestsPageTest() throws Exception {
        mockMvc.perform(get("/requests/all?from={from}&size={size}", 5, 5)
                        .header(Constants.USER_HEADER, 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(itemRequestClient).getAllItemRequests(1L, 5, 5);
    }

    @Test
    void getAllItemRequestsWrongPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/requests/all")
                        .param("from", "5")
                        .param("size", "-5") // Невалидный размер
                        .header(Constants.USER_HEADER, "1"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError()) // Ожидаем 500
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());

        Mockito.verify(itemRequestClient, Mockito.never())
                .getAllItemRequests(ArgumentMatchers.anyLong(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
    }

    ItemRequestDto getItemRequestDto() {
        return new ItemRequestDto("request");
    }
}
