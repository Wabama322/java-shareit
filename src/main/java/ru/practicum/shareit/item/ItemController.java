package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForBookingDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.utill.Constants;

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

    @PostMapping
    public ItemDto addItem(@RequestHeader(Constants.USER_HEADER) long userId,
                           @Valid @RequestBody ItemDto item) {
        log.info("Получил POST запрос на создание вещи от пользователя ID: {}", userId);
        return service.addItem(userId, item);
    }

    @PostMapping("/{item-id}/comment")
    public CommentDtoResponse addComment(@PathVariable("item-id") long itemId,
                                         @RequestHeader(Constants.USER_HEADER) long userId,
                                         @Valid @RequestBody CommentDtoRequest commentDtoRequest) {
        log.info("POST запрос на создание комментария к вещи ID: {} от пользователя ID: {}", itemId, userId);
        return service.addComment(itemId, userId, commentDtoRequest);
    }

    @PatchMapping("/{item-id}")
    public ItemDto updateItem(@RequestHeader(Constants.USER_HEADER) long userId,
                              @PathVariable("item-id") long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("Получил PATCH запрос на обновление вещи ID: {} от пользователя ID: {}", itemId, userId);
        return service.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{item-id}")
    public ItemForBookingDto getItem(@RequestHeader(Constants.USER_HEADER) Long userId,
                                     @PathVariable("item-id") Long itemId) {
        log.info("GET запрос на получение вещи ID: {} от пользователя ID: {}", itemId, userId);
        return service.getItemDto(userId, itemId);
    }

    @GetMapping
    public List<ItemForBookingDto> getAllItemsUser(@RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("GET запрос на получение всех вещей пользователя ID: {}", userId);
        return service.getAllItemsUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getSearchOfText(@RequestParam String text) {
        log.info("GET запрос на поиск вещей по тексту: {}", text);
        return service.getSearchOfText(text);
    }
}