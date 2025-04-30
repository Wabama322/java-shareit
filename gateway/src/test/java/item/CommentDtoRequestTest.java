package item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDtoRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenTextIsBlank_thenValidationFails() {
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text("")
                .build();

        Set<ConstraintViolation<CommentDtoRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("не должно быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenTextIsNull_thenValidationFails() {
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text(null)
                .build();

        Set<ConstraintViolation<CommentDtoRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("не должно быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenTextExceedsMaxLength_thenValidationFails() {
        String longText = "a".repeat(513);
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text(longText)
                .build();

        Set<ConstraintViolation<CommentDtoRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("размер должен находиться в диапазоне от 0 до 512", violations.iterator().next().getMessage());
    }

    @Test
    void whenTextIsValid_thenValidationSucceeds() {
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text("Valid comment text")
                .build();

        Set<ConstraintViolation<CommentDtoRequest>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBuilderAndAccessors() {
        String text = "Test comment";
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text(text)
                .build();

        assertEquals(text, dto.getText());
    }

    @Test
    void testEqualsAndHashCode() {
        CommentDtoRequest dto1 = CommentDtoRequest.builder().text("text").build();
        CommentDtoRequest dto2 = CommentDtoRequest.builder().text("text").build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text("text")
                .build();

        assertNotNull(dto.toString());
        assertTrue(dto.toString().contains("text"));
    }
}
