package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.utill.Constants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private static final String REQUEST_ID_PATH = "/{requestId}";

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestResponseDto createItemRequest(
            @RequestHeader(Constants.USER_HEADER) @Positive long userId,
            @Valid @RequestBody ItemRequestDto requestDto) {
        log.info("POST /requests - создание запроса пользователем {}", userId);
        ItemRequestResponseDto response = itemRequestService.addItemRequest(userId, requestDto);
        if (response.getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось создать запрос");
        }
        return response;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestResponseDto> getUserItemRequests(
            @RequestHeader(Constants.USER_HEADER) @Positive long userId) {
        log.info("GET /requests - получение запросов пользователя {}", userId);
        return itemRequestService.getItemRequestsByUserId(userId);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestResponseDto> getAllItemRequests(
            @RequestHeader(Constants.USER_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        log.info("GET /requests/all?from={}&size={} - получение всех запросов", from, size);
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    @GetMapping(REQUEST_ID_PATH)
    @ResponseStatus(HttpStatus.OK)
    public ItemRequestResponseDto getItemRequestById(
            @PathVariable long requestId,
            @RequestHeader(Constants.USER_HEADER) @Positive long userId) {
        log.info("GET /requests/{} - получение запроса пользователем {}", requestId, userId);
        return itemRequestService.getItemRequest(requestId, userId);
    }
}