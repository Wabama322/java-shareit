package item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenNameIsBlank_thenValidationFails() {
        ItemDto dto = ItemDto.builder()
                .name("")
                .description("description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Поле не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenDescriptionIsBlank_thenValidationFails() {
        ItemDto dto = ItemDto.builder()
                .name("name")
                .description("")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Поле не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenAvailableIsNull_thenValidationFails() {
        ItemDto dto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(null)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Поле не может быть null", violations.iterator().next().getMessage());
    }

    @Test
    void whenAllRequiredFieldsAreValid_thenValidationSucceeds() {
        ItemDto dto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBuilderAndAccessors() {
        Long id = 1L;
        String name = "Item";
        String description = "Description";
        Boolean available = true;
        Long ownerId = 2L;
        Long request = 3L;

        ItemDto dto = ItemDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .ownerId(ownerId)
                .request(request)
                .build();

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(available, dto.getAvailable());
        assertEquals(ownerId, dto.getOwnerId());
        assertEquals(request, dto.getRequest());
    }

    @Test
    void testEqualsAndHashCode() {
        ItemDto dto1 = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .build();

        ItemDto dto2 = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ItemDto dto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        assertNotNull(dto.toString());
        assertTrue(dto.toString().contains("name"));
        assertTrue(dto.toString().contains("description"));
    }
}
