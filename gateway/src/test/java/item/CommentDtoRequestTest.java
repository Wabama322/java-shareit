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
        assertFalse(violations.isEmpty(), "Validation should fail for blank text");
        assertEquals(1, violations.size(), "There should be exactly one validation error");
    }

    @Test
    void whenTextIsNull_thenValidationFails() {
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text(null)
                .build();

        Set<ConstraintViolation<CommentDtoRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Validation should fail for null text");
        assertEquals(1, violations.size(), "There should be exactly one validation error");
    }

    @Test
    void whenTextExceedsMaxLength_thenValidationFails() {
        String longText = "a".repeat(513);
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text(longText)
                .build();

        Set<ConstraintViolation<CommentDtoRequest>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Validation should fail for text exceeding max length");
        assertEquals(1, violations.size(), "There should be exactly one validation error");
    }

    @Test
    void whenTextIsValid_thenValidationSucceeds() {
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text("Valid comment text")
                .build();

        Set<ConstraintViolation<CommentDtoRequest>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Validation should pass for valid text");
    }

    @Test
    void testBuilderAndAccessors() {
        String text = "Test comment";
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text(text)
                .build();

        assertEquals(text, dto.getText(), "Text should match the builder value");
    }

    @Test
    void testEqualsAndHashCode() {
        CommentDtoRequest dto1 = CommentDtoRequest.builder().text("text").build();
        CommentDtoRequest dto2 = CommentDtoRequest.builder().text("text").build();

        assertEquals(dto1, dto2, "Objects with same values should be equal");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Hash codes should be equal for equal objects");
    }

    @Test
    void testToString() {
        CommentDtoRequest dto = CommentDtoRequest.builder()
                .text("text")
                .build();

        assertNotNull(dto.toString(), "toString() should not return null");
        assertTrue(dto.toString().contains("text"), "toString() should contain the text");
    }
}
