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

import java.nio.charset.StandardCharsets;

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
        when(userClient.postUser(any())).thenReturn(response);
        String result = mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(objectMapper.writeValueAsString(userDto), result);
    }

    @Test
    void addUserWithValidDataReturns200() throws Exception {
        UserDtoRequest validUser = new UserDtoRequest(1L, "Ilay", "valid@email.com");
        String validUserJson = objectMapper.writeValueAsString(validUser);

        when(userClient.postUser(any(UserDtoRequest.class)))
                .thenReturn(ResponseEntity.ok(validUserJson));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUserJson))
                .andExpect(status().isOk())
                .andExpect(content().json(validUserJson));

        verify(userClient, times(1)).postUser(any(UserDtoRequest.class));
    }

    @Test
    void addUserWithInvalidDataReturns500() throws Exception {
        UserDtoRequest invalidUser = new UserDtoRequest(null, "Alena", null);
        String invalidUserJson = objectMapper.writeValueAsString(invalidUser);

        when(userClient.postUser(any(UserDtoRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verify(userClient, never()).postUser(any(UserDtoRequest.class));
    }


    @Test
    void testPathUser() throws Exception {
        UserDtoRequest userDto = new UserDtoRequest(1L, "Ilay", "IlayLalala@gmail.com");
        UserDtoRequest userDtoUpdate = new UserDtoRequest(1L, "Alena", "AlenaNanana@gmail.com");
        String userJson = objectMapper.writeValueAsString(userDtoUpdate);
        ResponseEntity<Object> response = new ResponseEntity<>(userJson, HttpStatus.OK);
        when(userClient.patchUser(any(), anyLong())).thenReturn(response);
        String result = mockMvc.perform(
                        patch("/users/{userId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertEquals(objectMapper.writeValueAsString(userDtoUpdate), result);
    }

    @Test
    void getUserTest() throws Exception {
        int userId = 1;
        mockMvc.perform(get("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userClient).getUser(userId);
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
        int userId = 1;
        mockMvc.perform(delete("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userClient).delete(userId);
    }
}
