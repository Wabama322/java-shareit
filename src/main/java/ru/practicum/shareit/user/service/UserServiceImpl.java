package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.getAll();
    }

    @Override
    public UserDto getUser(long id) {
        userRepository.getUser(id)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь " + id + "не найден"));
        return UserMapper.toUserDto(userRepository.getUser(id).get());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        List<User> userList = getAllUsers().stream().collect(Collectors.toList());
        for (User user1 : userList) {
            if (user1.getEmail().equals(userDto.getEmail())) {
                throw new BadRequestException("Пользователь с таким email уже существует");
            }
        }
        return UserMapper.toUserDto(userRepository.addUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(long id, UserDto userDto) {
        List<User> userList = new ArrayList<>(getAllUsers());
        User user = userRepository.getUser(id)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь " + id + "не найден"));
        userList.remove(user);
        for (User user1 : userList) {
            if (user1.getEmail().equals(userDto.getEmail())) {
                throw new BadRequestException("Пользователь с таким email уже существует");
            }
        }
        return UserMapper.toUserDto(userRepository.updateUser(id, UserMapper.toUser(userDto)));
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteUser(id);
    }
}
