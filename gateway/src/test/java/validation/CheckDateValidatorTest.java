package validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.validation.CheckDateValidator;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CheckDateValidatorTest {
    private CheckDateValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new CheckDateValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void isValid_WhenAllFieldsValid_ReturnsTrue() {
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();

        assertTrue(validator.isValid(booking, context));
    }

    @Test
    void isValid_WhenStartIsNull_ReturnsFalse() {
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(null)
                .end(LocalDateTime.now())
                .itemId(1L)
                .build();

        assertFalse(validator.isValid(booking, context));
    }

    @Test
    void isValid_WhenEndIsNull_ReturnsFalse() {
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(LocalDateTime.now())
                .end(null)
                .itemId(1L)
                .build();

        assertFalse(validator.isValid(booking, context));
    }

    @Test
    void isValid_WhenItemIdIsNull_ReturnsFalse() {
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .itemId(null)
                .build();

        assertFalse(validator.isValid(booking, context));
    }

    @Test
    void isValid_WhenEndBeforeStart_ReturnsFalse() {
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now())
                .itemId(1L)
                .build();

        assertFalse(validator.isValid(booking, context));
    }

    @Test
    void isValid_WhenEndEqualsStart_ReturnsFalse() {
        LocalDateTime now = LocalDateTime.now();
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(now)
                .end(now)
                .itemId(1L)
                .build();

        assertFalse(validator.isValid(booking, context));
    }
}
