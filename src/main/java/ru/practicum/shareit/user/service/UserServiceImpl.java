package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Collection<User> getAllUsers() {
        log.debug("Запрос всех пользователей");
        return userRepository.getAll();
    }

    @Override
    public UserDto getUser(long id) {
        log.debug("Запрос пользователя с ID: {}", id);
        User user = userRepository.getUser(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new NotFoundException("Пользователь " + id + " не найден");
                });
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.debug("Создание нового пользователя с email: {}", userDto.getEmail());

        checkEmailUniqueness(userDto.getEmail());

        User newUser = UserMapper.toUser(userDto);
        User savedUser = userRepository.addUser(newUser);
        log.info("Создан новый пользователь с ID: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(long id, UserDto userDto) {
        log.debug("Обновление пользователя с ID: {}", id);

        User existingUser = userRepository.getUser(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден для обновления", id);
                    return new NotFoundException("Пользователь " + id + " не найден");
                });

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            checkEmailUniqueness(userDto.getEmail());
        }

        User updatedUser = UserMapper.toUser(userDto);
        updatedUser.setId(id);
        User savedUser = userRepository.updateUser(id, updatedUser);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public void deleteUser(long id) {
        log.debug("Удаление пользователя с ID: {}", id);
        if (!userRepository.getUser(id).isPresent()) {
            log.warn("Попытка удаления несуществующего пользователя с ID: {}", id);
            throw new NotFoundException("Пользователь " + id + " не найден");
        }
        userRepository.deleteUser(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }

    private void checkEmailUniqueness(String email) {
        boolean emailExists = userRepository.getAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));

        if (emailExists) {
            log.error("Попытка создания пользователя с существующим email: {}", email);
            throw new BadRequestException("Пользователь с таким email уже существует");
        }
    }
}

