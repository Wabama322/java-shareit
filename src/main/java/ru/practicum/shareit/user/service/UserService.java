package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@Service
public interface UserService {
    Collection<User> getAllUsers();

    UserDto getUser(long id);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(long id, UserDto userDto);

    void deleteUser(long id);
}
