package user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class UserMapperTest {

    @Test
    void toUserWithIdDtoMapperTest() {
        var original = User.builder()
                .id(1L)
                .name("name")
                .email("mail@gmail.com")
                .build();
        var result = UserMapper.toUserDtoResponse(original);

        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
    }

    @Test
    void toUserModelTest() {
        var original = new UserDto(1L, "Alena", "alena@mail.ru");

        var result = UserMapper.toUserModel(original);

        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getEmail(), result.getEmail());
    }
}
