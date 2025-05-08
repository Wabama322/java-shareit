package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.utill.Constants;
import ru.practicum.shareit.validation.Create;

import java.util.Collections;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;
    static final String PATH = "/{item-id}";

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(Constants.USER_HEADER) Long userId,
                                          @RequestBody @Validated({Create.class}) ItemDtoRequest itemDto) {
        log.info("POST запрос на создание вещи userId={}, itemDto={}", userId, itemDto);
        return itemClient.postItem(itemDto, userId);
    }

    @PostMapping("/{item-id}/comment")
    public ResponseEntity<Object> addComment(@PathVariable("item-id") Long itemId,
                                             @RequestHeader(Constants.USER_HEADER) Long userId,
                                             @Valid @RequestBody CommentDtoRequest commentDto) {
        log.info("POST запрос на создание комментария userId={}, itemId={}, commentDto={}", userId, itemId, commentDto);
        return itemClient.addComment(itemId, userId, commentDto);
    }

    @PatchMapping(PATH)
    public ResponseEntity<Object> updateItem(@RequestBody ItemDtoRequest itemDto,
                                             @RequestHeader(Constants.USER_HEADER) Long userId,
                                             @PathVariable("item-id") long itemId) {
        log.info("PATCH запрос на обновление вещи userId={}, itemId= {}, itemDto={}", userId, itemId, itemDto);
        return itemClient.patchItem(userId, itemId, itemDto);
    }

    @GetMapping(PATH)
    public ResponseEntity<Object> getItem(@PathVariable("item-id") Long itemId,
                                          @RequestHeader(Constants.USER_HEADER) Long ownerId) {
        log.info("GET запрос на получение вещи itemId={}, ownerId={}", itemId, ownerId);
        return itemClient.getItem(itemId, ownerId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsUser(@RequestHeader(Constants.USER_HEADER) Long userId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(defaultValue = "20") Integer size) {
        log.info("GET запрос на получение всех вещей пользователя userId={}, from={}, size={}", userId, from, size);
        return itemClient.getAllItemsUser(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getSearchOfText(@RequestHeader(Constants.USER_HEADER) long userId,
                                                  @RequestParam String text,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(defaultValue = "20") Integer size) {
        log.info("Получил GET запрос на получение всех вещей с текстом:={}, from={}, size={}", text, from, size);
        if (text == null || text.isBlank()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        return itemClient.getSearchOfText(userId, text, from, size);
    }
}
