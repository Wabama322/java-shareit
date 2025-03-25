package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.utill.Constants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> addItem(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @Valid @RequestBody ItemDto itemDto) {

        log.info("Создание вещи для пользователя {}: {}", userId, itemDto);
        ItemDto createdItem = itemService.addItem(userId, itemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader(Constants.USER_HEADER) long userId,
            @PathVariable long itemId,
            @RequestBody ItemDto itemDto) {

        log.info("Обновление вещи {} для пользователя {}", itemId, userId);
        return ResponseEntity.ok(itemService.updateItem(userId, itemId, itemDto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable long itemId) {
        log.info("Получение вещей: {}", itemId);
        return ResponseEntity.ok(itemService.getItemDto(itemId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItemsUser(
            @RequestHeader(Constants.USER_HEADER) long userId) {

        log.info("Получение всех вещей для пользователя: {}", userId);
        return ResponseEntity.ok(itemService.getAllItemsUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestParam String text) {

        log.info("Поиск вещей с текстом: '{}'", text);
        return ResponseEntity.ok(itemService.getSearchOfText(text));
    }
}