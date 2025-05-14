package user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository repository;

    private final User user = new User(1L, "Alena", "alena@mail.ru");
    private final UserDto userDtoRequest = new UserDto(1L, "Alena", "alena@mail.ru");
    private final UserDto userDtoResponse = new UserDto(1L, "Alena", "alena@mail.ru");
    private final User user2 = new User(2L,  "Nasty", "nasty@yandex.ru");
    private final UserDto userDtoRequest2 = new UserDto(2L, "Nasty", "nasty@yandex.ru");
    private final UserDto userDtoResponse2 = new UserDto(2L, "Nasty", "nasty@yandex.ru");


    @Test
    public void createUserTest() {
        when(repository.save(any()))
                .thenReturn(user);

        assertThat(userService.createUser(userDtoRequest), equalTo(userDtoResponse));
    }

    @Test
    public void getUserByIdExistTest() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        assertThat(userDtoResponse, equalTo(userService.getUser(1L)));
    }

    @Test
    public void getUserByIdNotExistTest() {
        when(repository.findById(anyLong()))
                .thenReturn(empty());

        Exception exception = Assertions.assertThrows(NotFoundException.class,
                () -> userService.getUser(1L));
        assertThat(exception.getMessage(), equalTo("Пользователь с id " +
                1 + " не найден"));
    }

    @Test
    public void getAllUsersTest() {
        when(repository.findAll())
                .thenReturn(List.of(user, user2));
        Collection<UserDto> users = userService.getAllUsers();

        assertThat(users, equalTo(List.of(userDtoResponse, userDtoResponse2)));
    }

    @Test
    public void findByIdThrowNotFoundExceptionTest() {
        when(repository.findById(anyLong()))
                .thenReturn(empty());

        Exception exception = Assertions.assertThrows(NotFoundException.class,
                () -> userService.getUser(1L));
        assertThat(exception.getMessage(), equalTo("Пользователь с id " +
                1 + " не найден"));
    }

    @Test
    public void deleteUserTest() {
        // Добавляем мок для проверки существования пользователя
        when(repository.existsById(anyLong())).thenReturn(true);

        userService.deleteUser(anyLong());
        verify(repository, times(1)).deleteById(anyLong());
    }

    @Test
    public void testToUserDto() {
        User user = new User(1L, "Sasha", "Sasha@gmail.com");

        UserDto userDto = UserMapper.toUserDto(user);
        assertEquals(1L, userDto.getId());
        assertEquals("Sasha", userDto.getName());
        assertEquals("Sasha@gmail.com", userDto.getEmail());

        User user2 = UserMapper.toUser(userDtoRequest2);
        assertEquals(2L, user2.getId());
        assertEquals("Nasty", user2.getName());
        assertEquals("nasty@yandex.ru", user2.getEmail());
    }
}