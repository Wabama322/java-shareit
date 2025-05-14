package user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDtoRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(classes = ShareItGateway.class)
public class UserControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserClient userClient;

    @Test
    void addUserTest() throws Exception {
        UserDtoRequest userDto = new UserDtoRequest(1L, "Ilay", "IlayLalala@gmail.com");
        String userJson = objectMapper.writeValueAsString(userDto);
        ResponseEntity<Object> response = new ResponseEntity<>(userJson, HttpStatus.OK);
        when(userClient.createUser(any())).thenReturn(response);

        String result = mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertEquals(userJson, result);
    }

    @Test
    void addUserWithValidDataReturns200() throws Exception {
        UserDtoRequest validUser = new UserDtoRequest(1L, "Ilay", "valid@email.com");
        String validUserJson = objectMapper.writeValueAsString(validUser);

        when(userClient.createUser(any(UserDtoRequest.class)))
                .thenReturn(ResponseEntity.ok(validUserJson));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUserJson))
                .andExpect(status().isOk())
                .andExpect(content().json(validUserJson));

        verify(userClient, times(1)).createUser(any(UserDtoRequest.class));
    }

    @Test
    void addUserWithInvalidDataReturns400() throws Exception {
        UserDtoRequest invalidUser = new UserDtoRequest(null, "Alena", null);
        String invalidUserJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(userClient, never()).createUser(any(UserDtoRequest.class));
    }

    @Test
    void testPathUser() throws Exception {
        UserDtoRequest userDto = new UserDtoRequest(1L, "Ilay", "IlayLalala@gmail.com");
        UserDtoRequest userDtoUpdate = new UserDtoRequest(1L, "Alena", "AlenaNanana@gmail.com");
        String userJson = objectMapper.writeValueAsString(userDtoUpdate);
        ResponseEntity<Object> response = new ResponseEntity<>(userJson, HttpStatus.OK);
        when(userClient.updateUser(any(), anyLong())).thenReturn(response);
        String result = mockMvc.perform(
                        patch("/users/{userId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertEquals(userJson, result);
    }

    @Test
    void getUserTest() throws Exception {
        long userId = 1L;
        mockMvc.perform(get("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userClient).getUserById(userId);
    }

    @Test
    void getAllUsersTest() throws Exception {
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userClient).getAllUsers();
    }

    @Test
    void deleteUserTest() throws Exception {
        long userId = 1L;
        mockMvc.perform(delete("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userClient).deleteUser(userId);
    }
}
