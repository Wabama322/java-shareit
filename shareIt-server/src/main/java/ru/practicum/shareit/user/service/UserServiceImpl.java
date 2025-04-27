package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDtoResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUser(long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id " +
                        id + " не найден"));
        return UserMapper.toUserDtoResponse(user);
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUserModel(userDto);
        User saveUser = userRepository.save(user);
        return UserMapper.toUserDtoResponse(saveUser);
    }

    @Transactional
    @Override
    public UserDto updateUser(long id, UserDto userDto) {
        User oldUser = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id " +
                        id + " не найден"));
        if (userDto.getEmail() != null) {
            oldUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().equals(oldUser.getName())) {
            oldUser.setName(userDto.getName());
        }
        User saveUser = userRepository.save(oldUser);
        return UserMapper.toUserDtoResponse(saveUser);
    }

    @Transactional
    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}
