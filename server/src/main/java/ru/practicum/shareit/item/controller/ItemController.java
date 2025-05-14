package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utill.Constants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String ITEM_ID_PATH = "/{itemId}";

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDtoResponse createItem(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @Valid @RequestBody ItemDtoRequest request) {
        log.info("POST /items - создание вещи пользователем {}", userId);
        return itemService.addItem(userId, request);
    }

    @PostMapping(ITEM_ID_PATH + "/comment")
    @ResponseStatus(HttpStatus.OK)
    public CommentDtoResponse addCommentToItem(
            @PathVariable long itemId,
            @RequestHeader(Constants.USER_HEADER) long userId,
            @Valid @RequestBody CommentDtoRequest request) {
        log.info("POST /items/{}/comment - добавление комментария пользователем {}", itemId, userId);
        return itemService.addComment(itemId, userId, request);
    }

    @PatchMapping(ITEM_ID_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ItemDtoResponse updateItem(
            @PathVariable long itemId,
            @RequestHeader(Constants.USER_HEADER) long userId,
            @Valid @RequestBody ItemDtoRequest request) {
        log.info("PATCH /items/{} - обновление вещи пользователем {}", itemId, userId);
        return itemService.updateItem(userId, itemId, request);
    }

    @GetMapping(ITEM_ID_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ItemForBookingDto getItemById(
            @PathVariable long itemId,
            @RequestHeader(Constants.USER_HEADER) Long ownerId) {
        log.info("GET /items/{} - получение вещи", itemId);
        return itemService.getItemDto(ownerId, itemId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemForBookingDto> getAllUserItems(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        log.info("GET /items - получение всех вещей пользователя {}", userId);
        return itemService.getAllItemsUser(userId, from, size);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemSearchOfTextDto> searchItemsByText(
            @RequestParam @NotBlank String text,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        log.info("GET /items/search?text={} - поиск вещей", text);
        return itemService.searchItems(text.trim(), from, size);
    }
}