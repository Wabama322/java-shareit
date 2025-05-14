package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.utill.Constants;
import ru.practicum.shareit.validation.Create;

import java.util.Collections;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private static final String ITEM_ID_PATH = "/{itemId}";
    private static final String COMMENT_PATH = ITEM_ID_PATH + "/comment";
    private static final String SEARCH_PATH = "/search";
    private static final String DEFAULT_FROM = "0";
    private static final String DEFAULT_SIZE = "20";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader(Constants.USER_HEADER) Long userId,
            @RequestBody @Validated(Create.class) ItemDtoRequest itemDto) {
        log.debug("Create item request. UserId: {}, ItemDto: {}", userId, itemDto);
        return itemClient.createItem(itemDto, userId);
    }

    @PostMapping(COMMENT_PATH)
    public ResponseEntity<Object> addComment(
            @PathVariable Long itemId,
            @RequestHeader(Constants.USER_HEADER) Long userId,
            @Valid @RequestBody CommentDtoRequest commentDto) {
        log.debug("Add comment request. UserId: {}, ItemId: {}", userId, itemId);
        return itemClient.addComment(itemId, userId, commentDto);
    }

    @PatchMapping(ITEM_ID_PATH)
    public ResponseEntity<Object> updateItem(
            @RequestBody ItemDtoRequest itemDto,
            @RequestHeader(Constants.USER_HEADER) Long userId,
            @PathVariable Long itemId) {
        log.debug("Update item request. UserId: {}, ItemId: {}", userId, itemId);
        return itemClient.updateItem(itemId, userId, itemDto);
    }

    @GetMapping(ITEM_ID_PATH)
    public ResponseEntity<Object> getItemById(
            @PathVariable Long itemId,
            @RequestHeader(Constants.USER_HEADER) Long userId) {
        log.debug("Get item request. ItemId: {}, UserId: {}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(
            @RequestHeader(Constants.USER_HEADER) Long userId,
            @PositiveOrZero @RequestParam(defaultValue = DEFAULT_FROM) Integer from,
            @Positive @RequestParam(defaultValue = DEFAULT_SIZE) Integer size) {
        log.debug("Get user items request. UserId: {}, From: {}, Size: {}", userId, from, size);
        return itemClient.getUserItems(userId, from, size);
    }

    @GetMapping(SEARCH_PATH)
    public ResponseEntity<Object> searchItems(
            @RequestHeader(Constants.USER_HEADER) Long userId,
            @RequestParam String text,
            @PositiveOrZero @RequestParam(defaultValue = DEFAULT_FROM) Integer from,
            @Positive @RequestParam(defaultValue = DEFAULT_SIZE) Integer size) {

        if (text.isBlank()) {
            log.debug("Empty search text - returning empty list");
            return ResponseEntity.ok(Collections.emptyList());
        }

        log.debug("Search items request. Text: {}, From: {}, Size: {}", text, from, size);
        return itemClient.searchItems(text, userId, from, size);
    }
}