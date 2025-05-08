package item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.validation.Create;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenCreateGroupAndNameIsBlank_thenValidationFails() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("")
                .description("description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDtoRequest>> violations = validator.validate(dto, Create.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void whenCreateGroupAndDescriptionIsBlank_thenValidationFails() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("name")
                .description("")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDtoRequest>> violations = validator.validate(dto, Create.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void whenCreateGroupAndAvailableIsNull_thenValidationFails() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("name")
                .description("description")
                .available(null)
                .build();

        Set<ConstraintViolation<ItemDtoRequest>> violations = validator.validate(dto, Create.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void whenNotCreateGroup_thenValidationSucceedsEvenWithInvalidFields() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        Set<ConstraintViolation<ItemDtoRequest>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenAllRequiredFieldsAreValid_thenValidationSucceeds() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDtoRequest>> violations = validator.validate(dto, Create.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBuilderAndAccessors() {
        Long id = 1L;
        String name = "Item";
        String description = "Description";
        Boolean available = true;
        Long requestId = 2L;

        ItemDtoRequest dto = ItemDtoRequest.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .requestId(requestId)
                .build();

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(available, dto.getAvailable());
        assertEquals(requestId, dto.getRequestId());
    }

    @Test
    void testEqualsAndHashCode() {
        ItemDtoRequest dto1 = ItemDtoRequest.builder()
                .id(1L)
                .name("name")
                .description("desc")
                .available(true)
                .build();

        ItemDtoRequest dto2 = ItemDtoRequest.builder()
                .id(1L)
                .name("name")
                .description("desc")
                .available(true)
                .build();

        assertEquals(dto1, dto1);
        assertEquals(dto2, dto2);

        if (dto1.equals(dto2)) {
            assertTrue(dto2.equals(dto1));
        }

        if (dto1.equals(dto2)) {
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        ItemDtoRequest different = ItemDtoRequest.builder()
                .id(999L)
                .name("different")
                .description("different")
                .available(false)
                .build();

        assertNotEquals(dto1, different);
    }

    @Test
    void testToString() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .name("testName")
                .description("testDescription")
                .available(true)
                .build();

        String result = dto.toString();

        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertTrue(result.contains("ItemDtoRequest"));
    }
}
