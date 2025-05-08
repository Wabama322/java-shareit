package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    static final String path = "/{user-id}";

    @GetMapping
    public Collection<UserDto> getAll() {
        log.info("GET запрос на получение всех пользователей");
        return service.getAllUsers();
    }

    @GetMapping(path)
    public UserDto getUser(@PathVariable("user-id") long userId) {
        log.info("GET запрос на получение пользователя");
        return service.getUser(userId);
    }

    @PostMapping
    public UserDto addUser(@RequestBody UserDto userDtoRequest) {
        log.info("POST запрос на создание пользователя");
        return service.createUser(userDtoRequest);
    }

    @PatchMapping(path)
    public UserDto updateUser(@PathVariable("user-id") long userId, @RequestBody UserDto userDtoRequest) {
        log.info("PATCH запрос на обновление пользователя");
        return service.updateUser(userId, userDtoRequest);
    }

    @DeleteMapping(path)
    public void deleteUser(@PathVariable("user-id") long userId) {
        log.info("DELETE запрос на удаление пользователя с ID: {}", userId);
        service.deleteUser(userId);
    }
}
