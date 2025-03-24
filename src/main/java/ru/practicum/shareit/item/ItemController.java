package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;
    static final String userHeader = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto addItem(@RequestHeader(userHeader) long userId, @Valid @RequestBody ItemDto item) {
        log.info("Получил POST запрос на создание вещи");
        return service.addItem(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(userHeader) long userId, @PathVariable long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("Получил PATCH запрос на обновление вещи");
        return service.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId) {
        log.info("Получил GET запрос на получение вещи с id: {}", itemId);
        return service.getItemDto(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsUser(@RequestHeader(userHeader) long userId) {
        log.info("Получил GET запрос на получение всех вещей пользователя с id: {}", userId);
        return service.getAllItemsUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getSearchOfText(@RequestParam String text) {
        log.info("Получил GET запрос на получение всех вещей с текстом: {}", text);
        return service.getSearchOfText(text);
    }
}