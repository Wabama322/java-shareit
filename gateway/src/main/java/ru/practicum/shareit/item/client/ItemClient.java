package ru.practicum.shareit.item.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";
    private static final String FROM_PARAM = "from";
    private static final String SIZE_PARAM = "size";
    private static final String TEXT_PARAM = "text";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(buildRestTemplate(serverUrl, builder));
    }

    private static RestTemplate buildRestTemplate(String serverUrl, RestTemplateBuilder builder) {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .build();
    }

    public ResponseEntity<Object> createItem(ItemDtoRequest itemDto, Long userId) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long itemId, Long userId, ItemDtoRequest itemDto) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long itemId, Long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getUserItems(Long userId, Integer from, Integer size) {
        return getWithPagination("", userId, null, from, size);
    }

    public ResponseEntity<Object> searchItems(String text, Long userId, Integer from, Integer size) {
        return getWithPagination("/search", userId, text, from, size);
    }

    public ResponseEntity<Object> addComment(Long itemId, Long userId, CommentDtoRequest commentDto) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }

    private ResponseEntity<Object> getWithPagination(String path, Long userId,
                                                     String text, Integer from, Integer size) {
        Map<String, Object> parameters = new HashMap<>();
        if (text != null) {
            parameters.put(TEXT_PARAM, text);
        }
        parameters.put(FROM_PARAM, from);
        parameters.put(SIZE_PARAM, size);

        String query = parameters.keySet().stream()
                .map(key -> key + "={" + key + "}")
                .collect(Collectors.joining("&"));

        return get(path + "?" + query, userId, parameters);
    }
}