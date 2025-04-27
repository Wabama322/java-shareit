package validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;


import java.time.LocalDateTime;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckDateValidatorTest {
    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testCheckDateValidatorNull() {
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(null)
                .end(null)
                .itemId(null)
                .build();

        Set<ConstraintViolation<BookingDtoRequest>> violations = validator.validate(booking);

        assertTrue(violations.isEmpty(),
                "При отсутствии аннотаций валидации violations должен быть пустым");
    }

    @Test
    public void testCheckDateValidatorNullStart() {
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(null)
                .end(LocalDateTime.now())
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDtoRequest>> violations = validator.validate(booking);

        assertTrue(violations.isEmpty(),
                "При отсутствии аннотаций валидации violations должен быть пустым");
    }

    @Test
    public void testCheckDateValidatorValidData() {
        BookingDtoRequest booking = BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDtoRequest>> violations = validator.validate(booking);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testCheckDateValidatorStartAfter() {
        BookingDtoRequest bookItemRequestDto = BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusNanos(2))
                .end(LocalDateTime.now())
                .itemId(1L)
                .build();
        Set<ConstraintViolation<BookingDtoRequest>> violations = validator.validate(bookItemRequestDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testCheckDateValidatorNullEnd() {
        BookingDtoRequest bookItemRequestDto = BookingDtoRequest.builder()
                .start(LocalDateTime.now())
                .end(null)
                .itemId(1L)
                .build();
        Set<ConstraintViolation<BookingDtoRequest>> violations = validator.validate(bookItemRequestDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testCheckDateValidatorEndBefore() {
        BookingDtoRequest bookItemRequestDto = BookingDtoRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().minusDays(1))
                .itemId(1L)
                .build();
        Set<ConstraintViolation<BookingDtoRequest>> violations = validator.validate(bookItemRequestDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testCheckDateValidatorNullItemId() {
        BookingDtoRequest bookItemRequestDto = BookingDtoRequest.builder()
                .start(LocalDateTime.now().minusWeeks(1))
                .end(LocalDateTime.now().minusDays(1))
                .itemId(null)
                .build();
        Set<ConstraintViolation<BookingDtoRequest>> violations = validator.validate(bookItemRequestDto);
        assertFalse(violations.isEmpty());
    }
}
