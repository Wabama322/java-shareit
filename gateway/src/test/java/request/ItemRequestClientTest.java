package request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        itemRequestClient = new ItemRequestClient(serverUrl, builder);
    }

    @Test
    void addItemRequest_ShouldCallPostWithCorrectParameters() {
        ResponseEntity<Object> mockResponse = mock(ResponseEntity.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(mockResponse);

        ResponseEntity<Object> response = itemRequestClient.addItemRequest(userId, itemRequestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getItemRequest_ShouldCallGetWithCorrectPath() {
        ResponseEntity<Object> mockResponse = mock(ResponseEntity.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(mockResponse);

        ResponseEntity<Object> response = itemRequestClient.getItemRequest(requestId, userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getItemRequestsByUserId_ShouldCallGetWithCorrectPath() {
        ResponseEntity<Object> mockResponse = mock(ResponseEntity.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(mockResponse);

        ResponseEntity<Object> response = itemRequestClient.getItemRequestsByUserId(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllItemRequests_ShouldCallGetWithParameters() {
        ResponseEntity<Object> mockResponse = mock(ResponseEntity.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                any(Map.class)
        )).thenReturn(mockResponse);

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
