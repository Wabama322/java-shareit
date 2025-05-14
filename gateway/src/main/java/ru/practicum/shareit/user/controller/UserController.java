package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.validation.Create;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private static final String USER_ID_PATH = "/{userId}";

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(
            @RequestBody @Validated(Create.class) UserDtoRequest userDto) {
        log.debug("Creating user. Email: {}", userDto.getEmail());
        return userClient.createUser(userDto);
    }

    @GetMapping(USER_ID_PATH)
    public ResponseEntity<Object> getUser(
            @PathVariable Long userId) {
        log.debug("Fetching user. ID: {}", userId);
        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.debug("Fetching all users");
        return userClient.getAllUsers();
    }

    @PatchMapping(USER_ID_PATH)
    public ResponseEntity<Object> updateUser(
            @RequestBody @Valid UserDtoRequest userDto,
            @PathVariable Long userId) {
        log.debug("Updating user. ID: {}, Name: {}", userId, userDto.getName());
        return userClient.updateUser(userDto, userId);
    }

    @DeleteMapping(USER_ID_PATH)
    public ResponseEntity<Object> removeUser(
            @PathVariable Long userId) {
        log.debug("Removing user. ID: {}", userId);
        return userClient.deleteUser(userId);
    }
}