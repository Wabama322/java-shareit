package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    private static final String USER_ID_PATH = "/{userId}";

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        log.info("GET /users - получение всех пользователей");
        return service.getAllUsers();
    }

    @GetMapping(USER_ID_PATH)
    public UserDto getUserById(@PathVariable long userId) {
        log.info("GET /users/{} - получение пользователя", userId);
        return service.getUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDtoRequest) {
        log.info("POST /users - создание нового пользователя");
        return service.createUser(userDtoRequest);
    }

    @PatchMapping(USER_ID_PATH)
    public UserDto updateUser(
            @PathVariable long userId,
            @Valid @RequestBody UserDto userDtoRequest
    ) {
        log.info("PATCH /users/{} - обновление пользователя", userId);
        return service.updateUser(userId, userDtoRequest);
    }

    @DeleteMapping(USER_ID_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        service.deleteUser(userId);
    }
}