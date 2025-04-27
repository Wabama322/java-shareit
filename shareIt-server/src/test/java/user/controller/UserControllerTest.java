package user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = ShareItServer.class)
public class UserControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mockMvc;

    private static final String PATH_USERS = "/users";
    User user = User.builder()
            .id(1L).name("Alena")
            .email("alena@mail.ru")
            .build();

    UserDto userDtoRequest = UserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();
    UserDto userDtoResponse = UserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();

    @SneakyThrows
    @Test
    void addUserTest() throws Exception {
        when(userService.createUser(any(UserDto.class)))
                .thenReturn(userDtoResponse);

        mockMvc.perform(post(PATH_USERS)
                        .content(mapper.writeValueAsString(userDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoResponse.getName())))
                .andExpect(jsonPath("$.email", is(userDtoResponse.getEmail())));
    }

    @SneakyThrows
    @Test
    public void getAllUsersTest() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDtoResponse));

        String result = mockMvc.perform(get(PATH_USERS))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(mapper.writeValueAsString(List.of(userDtoResponse)), result);
    }

    @SneakyThrows
    @Test
    public void getUsersByIdTest() throws Exception {
        when(userService.getUser(anyLong())).thenReturn(userDtoResponse);

        mockMvc.perform(get("/users/{id}", userDtoRequest.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoResponse.getName())))
                .andExpect(jsonPath("$.email", is(userDtoResponse.getEmail())));
    }

    @SneakyThrows
    @Test
    void updateUserTest() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(userDtoResponse);

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDtoResponse)));
    }

    @SneakyThrows
    @Test
    void deleteUserTestTest() throws Exception {
        mockMvc.perform(delete(PATH_USERS + "/1"))
                .andExpect(status().isOk());
        verify(userService, times(1))
                .deleteUser(anyLong());
    }

    @AfterEach
    void deleteUser() {
        userService.deleteUser(anyLong());
    }
}
