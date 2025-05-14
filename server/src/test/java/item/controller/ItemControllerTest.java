package item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utill.Constants;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = ShareItServer.class)
public class ItemControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;
    @Autowired
    MockMvc mockMvc;
    ItemDtoRequest itemDtoRequest;
    ItemDtoResponse itemDtoResponse;
    ItemRequest itemRequest;
    Item item;
    ItemSearchOfTextDto itemSearchOfTextDto;
    ItemForBookingDto itemWithBookingAndCommentsDto;
    User owner;
    User booker;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("name user")
                .email("mail@mall.ru")
                .build();

        booker = User.builder()
                .id(101L)
                .name("name booker")
                .email("booker@email.ru")
                .build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("doll")
                .requester(booker)
                .created(LocalDateTime.now())
                .build();

        item = Item.builder()
                .id(1L)
                .name("Baby doll")
                .description("baby doll for children")
                .available(true)
                .owner(owner)
                .request(itemRequest)
                .build();

        itemRequest.setItems(List.of(item));

        itemDtoRequest = ItemDtoRequest.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(itemRequest.getId())
                .build();

        itemDtoResponse = ItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(itemRequest.getId())
                .build();

        itemWithBookingAndCommentsDto = ItemForBookingDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .build();

        itemSearchOfTextDto = ItemSearchOfTextDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();

        assertEquals(item.getId(), itemDtoRequest.getId());
        assertEquals(item.getName(), itemDtoRequest.getName());
        assertEquals(item.getDescription(), itemDtoRequest.getDescription());
        assertEquals(item.getAvailable(), itemDtoRequest.getAvailable());
    }

    @Test
    void testGetItemByIdForOwnerTest() throws Exception {
        when(itemService.getItemDto(anyLong(), anyLong()))
                .thenReturn(itemWithBookingAndCommentsDto);

        mockMvc.perform(get("/items/{id}", itemDtoRequest.getId())
                        .header(Constants.USER_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingAndCommentsDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemWithBookingAndCommentsDto.getDescription()), String.class))
                .andExpect(jsonPath("$.name", is(itemWithBookingAndCommentsDto.getName()), String.class));
    }

    @Test
    void testAddTest() throws Exception {
        when(itemService.addItem(anyLong(), any(ItemDtoRequest.class)))
                .thenReturn(itemDtoResponse);

        mockMvc.perform(post("/items")
                        .header(Constants.USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoRequest))) // Исправлено: передаем request, а не response
                .andExpect(status().isCreated()) // Изменено с isOk() на isCreated()
                .andExpect(jsonPath("$.id", is(itemDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription()), String.class))
                .andExpect(jsonPath("$.requestId", is(itemDtoResponse.getRequestId()), Long.class))
                .andExpect(jsonPath("$.available", is(itemDtoResponse.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.name", is(itemDtoResponse.getName()), String.class));
    }

    @Test
    void testUpdateWhenAllAreOkAndReturnUpdatedItemTest() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDtoRequest.class)))
                .thenReturn(itemDtoResponse);

        mockMvc.perform(patch("/items/{itemId}", itemDtoRequest.getId())
                        .header(Constants.USER_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription()), String.class))
                .andExpect(jsonPath("$.requestId", is(itemDtoResponse.getRequestId()), Long.class))
                .andExpect(jsonPath("$.available", is(itemDtoResponse.getAvailable()), Boolean.class))
                .andExpect(jsonPath("$.name", is(itemDtoResponse.getName()), String.class));
    }

    @Test
    void addCommentToItemTest() throws Exception {
        CommentDtoRequest commentDto = CommentDtoRequest.builder()
                .text("comment 1")
                .build();
        CommentDtoResponse commentDtoResponse = CommentDtoResponse.builder()
                .id(1L)
                .text("comment 1")
                .authorName("name user")
                .created(LocalDateTime.now().minusSeconds(5))
                .build();
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDtoRequest.class)))
                .thenReturn(commentDtoResponse);

        mockMvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header(Constants.USER_HEADER, "1")
                        .content(objectMapper.writeValueAsString(commentDtoResponse))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDtoResponse.getText()), String.class))
                .andExpect(jsonPath("$.authorName", is(commentDtoResponse.getAuthorName()), String.class));
    }
}