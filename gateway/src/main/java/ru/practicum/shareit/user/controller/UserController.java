package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
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
    private static final String PATH_VARIABLE = "/{userId}";

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(
            @RequestBody @Validated({Create.class}) UserDtoRequest userDto) {
        log.info("Creating user: {}", userDto);
        return userClient.createUser(userDto);
    }

    @GetMapping(PATH_VARIABLE)
    public ResponseEntity<Object> getUserById(
            @PathVariable("userId") long userId) {
        log.info("Getting user by ID: {}", userId);
        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Getting all users");
        return userClient.getAllUsers();
    }

    @PatchMapping(PATH_VARIABLE)
    public ResponseEntity<Object> updateUser(
            @RequestBody @Valid UserDtoRequest userDto,
            @PathVariable("userId") long userId) {
        log.info("Updating user ID {} with data: {}", userId, userDto);
        return userClient.updateUser(userDto, userId);
    }

    @DeleteMapping(PATH_VARIABLE)
    public ResponseEntity<Object> deleteUser(
            @PathVariable("userId") long userId) {
        log.info("Deleting user ID: {}", userId);
        return userClient.deleteUser(userId);
    }
}
