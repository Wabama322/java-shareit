package validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.validation.CheckDateValidator;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CheckDateValidatorTest {
    private CheckDateValidator validator;

    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new CheckDateValidator();
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
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(null)
                .end(LocalDateTime.now())
                .itemId(1L)
                .build();

        assertFalse(validator.isValid(booking, context));
    }

    @Test
    void isValid_WhenEndIsNull_ReturnsFalse() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(LocalDateTime.now())
                .end(null)
                .itemId(1L)
                .build();

        assertFalse(validator.isValid(booking, context));
    }

    @Test
    void isValid_WhenItemIdIsNull_ReturnsFalse() {
        when(context.buildConstraintViolationWithTemplate("ID вещи должен быть указан"))
                .thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .itemId(null)
                .build();

        assertFalse(validator.isValid(booking, context));

        verify(context).buildConstraintViolationWithTemplate("ID вещи должен быть указан");
        verify(builder).addConstraintViolation();
    }

    @Test
    void isValid_WhenEndBeforeStart_ReturnsFalse() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now())
                .itemId(1L)
                .build();

        assertFalse(validator.isValid(booking, context));
    }

    @Test
    void isValid_WhenEndEqualsStart_ReturnsFalse() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        LocalDateTime now = LocalDateTime.now();
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(now)
                .end(now)
                .itemId(1L)
                .build();

        assertFalse(validator.isValid(booking, context));
    }
}