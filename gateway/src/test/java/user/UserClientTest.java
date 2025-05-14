package user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDtoRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientTest {
    private static final String SERVER_URL = "http://localhost:8080";
    private static final long USER_ID = 1L;
    private static final UserDtoRequest USER_DTO = new UserDtoRequest(1L, "Test User", "test@email.com");

    @Mock
    private RestTemplate restTemplate;
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = Mockito.mock(RestTemplateBuilder.class);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        userClient = new UserClient(SERVER_URL, builder);
    }

    @Test
    void createUser_ShouldCallPost() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.createUser(USER_DTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateUser_ShouldCallPatch() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PATCH),
                any(),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.updateUser(USER_DTO, USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteUser_ShouldCallDelete() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.deleteUser(USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getUserById_ShouldCallGet() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.getUserById(USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllUsers_ShouldCallGet() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createUser_ShouldReturnErrorResponseWhenServerError() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(Object.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<Object> response = userClient.createUser(USER_DTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}