package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.validation.Create;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;
    static final String path = "/{user-id}";

    @PostMapping
    public ResponseEntity<Object> addUser(@RequestBody @Validated({Create.class}) UserDtoRequest userDto) {
        log.info("POST запрос на создание пользователя {}", userDto);
        return userClient.postUser(userDto);
    }

    @GetMapping(path)
    public ResponseEntity<Object> getUser(@PathVariable("user-id") Long userId) {
        log.info("GET запрос на получение пользователя userId={}", userId);
        return userClient.getUser(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("GET запрос на получение всех пользователей");
        return userClient.getAllUsers();
    }

    @PatchMapping(path)
    public ResponseEntity<Object> updateUser(@RequestBody UserDtoRequest userDto,
                                             @PathVariable("user-id") long userId) {
        log.info("PATCH запрос на обновление пользователя userId={}, userDto={}", userId, userDto);
        return userClient.patchUser(userDto, userId);
    }

    @DeleteMapping(path)
    public ResponseEntity<Object> deleteUser(@PathVariable("user-id") long userId) {
        log.info("DELETE запрос на удаление пользователя userId={}", userId);
        return userClient.delete(userId);
    }
}
