package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.utill.Constants;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private static final String DEFAULT_FROM = "0";
    private static final String DEFAULT_SIZE = "20";
    private static final String REQUEST_ID_PATH = "/{requestId}";

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(
            @RequestHeader(Constants.USER_HEADER) Long userId,
            @Valid @RequestBody ItemRequestDto requestDto) {
        log.debug("Create request. UserId: {}", userId);
        return itemRequestClient.addItemRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(
            @RequestHeader(Constants.USER_HEADER) Long userId) {
        log.debug("Get user requests. UserId: {}", userId);
        return itemRequestClient.getItemRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader(Constants.USER_HEADER) Long userId,
            @PositiveOrZero @RequestParam(defaultValue = DEFAULT_FROM) Integer from,
            @Positive @RequestParam(defaultValue = DEFAULT_SIZE) Integer size) {
        log.debug("Get all requests. UserId: {}, From: {}, Size: {}", userId, from, size);
        return itemRequestClient.getAllItemRequests(userId, from, size);
    }

    @GetMapping(REQUEST_ID_PATH)
    public ResponseEntity<Object> getRequestById(
            @PathVariable("requestId") Long requestId,
            @RequestHeader(Constants.USER_HEADER) Long userId) {
        log.debug("Get request by ID. RequestId: {}, UserId: {}", requestId, userId);
        return itemRequestClient.getItemRequest(requestId, userId);
    }
}