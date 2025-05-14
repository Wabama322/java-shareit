package request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemRequestClientTest {

    @Mock
    private RestTemplate restTemplate;
    private ItemRequestClient itemRequestClient;

    private final String serverUrl = "http://test-server:8080";
    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final Integer from = 0;
    private final Integer size = 10;
    private final ItemRequestDto itemRequestDto = new ItemRequestDto();

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        itemRequestClient = new ItemRequestClient(serverUrl, builder);
    }

    @Test
    void addItemRequest_ShouldCallPostWithCorrectParameters() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemRequestClient.addItemRequest(userId, itemRequestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getItemRequest_ShouldCallGetWithCorrectPath() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemRequestClient.getItemRequest(requestId, userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getItemRequestsByUserId_ShouldCallGetWithCorrectPath() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemRequestClient.getItemRequestsByUserId(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllItemRequests_ShouldCallGetWithParameters() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemRequestClient.getAllItemRequests(userId, from, size);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addItemRequest_ShouldHandleClientError() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(Object.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        ResponseEntity<Object> response = itemRequestClient.addItemRequest(userId, itemRequestDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}