package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable long userId) {
        log.info("Получил GET запрос на получение пользователя");
        return service.getUser(userId);
    }

    @PostMapping
    public UserDto addUser(@RequestBody @Valid UserDto userDto) {
        log.info("Получил POST запрос на создание пользователя");
        return service.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable long userId, @RequestBody  UserDto userDto) {
        log.info("Получил PATCH запрос на обновление пользователя");
        return service.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        log.info("Получил DELETE запрос на удаление пользователя с id: {}", userId);
        service.deleteUser(userId);
    }
}
