package user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.validation.Create;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoRequestTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void builder_ShouldCreateValidUserDto() {
        UserDtoRequest user = UserDtoRequest.builder()
                .id(1L)
                .name("Vitya")
                .email("Vitya@google.com")
                .build();

        assertEquals(1L, user.getId());
        assertEquals("Vitya", user.getName());
        assertEquals("Vitya@google.com", user.getEmail());
    }

    @Test
    void validation_CreateGroup_ShouldFailOnEmptyNameAndInvalidEmail() {
        UserDtoRequest user = UserDtoRequest.builder()
                .id(1L)
                .name(" ")
                .email("invalid-email")
                .build();

        Set<ConstraintViolation<UserDtoRequest>> violations = validator.validate(user, Create.class);
        assertEquals(2, violations.size());
    }

    @Test
    void equalsAndHashCode_ShouldDependOnlyOnNameAndEmail() {
        UserDtoRequest user1 = UserDtoRequest.builder()
                .id(1L)
                .name("Egor")
                .email("Egor@google.com")
                .build();

        UserDtoRequest user2 = UserDtoRequest.builder()
                .id(2L)
                .name("Egor")
                .email("Egor@google.com")
                .build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void validation_ShouldPassWithValidEmail() {
        UserDtoRequest user = UserDtoRequest.builder()
                .id(1L)
                .name("Roma")
                .email("Roma@google.com")
                .build();

        Set<ConstraintViolation<UserDtoRequest>> violations = validator.validate(user, Create.class);
        assertTrue(violations.isEmpty());
    }
}
