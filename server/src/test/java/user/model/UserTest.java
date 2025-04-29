package user.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserTest {
    @Test
    void userHashCodeTest() {
        User user1 = User.builder()
                .id(1L)
                .name("name")
                .email("user1@mail.ru").build();

        User user2 = User.builder()
                .id(1L)
                .name("name")
                .email("user1@mail.ru").build();

        User user3 = User.builder()
                .id(1L)
                .name("alena")
                .email("user1@mail.ru").build();

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
    }
}
