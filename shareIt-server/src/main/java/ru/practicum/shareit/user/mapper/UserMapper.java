package ru.practicum.shareit.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserWithIdDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class UserMapper {
    public UserWithIdDto toUserWithIdDtoMapper(User user) {
        return new UserWithIdDto(user.getId());
    }

    public User toUserModel(UserDto userDtoRequest) {
        return new User(
                userDtoRequest.getId(),
                userDtoRequest.getName(),
                userDtoRequest.getEmail()
        );
    }

    public UserDto toUserDtoResponse(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
