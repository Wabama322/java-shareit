package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.BooleanFlag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.ItemRequest;

/**
 * TODO Sprint add-controllers.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    Long id;
    @NotBlank(message = "Поле не может быть пустым")
    String name;
    @NotBlank(message = "Поле не может быть пустым")
    String description;
    @BooleanFlag
    @NotNull(message = "Поле не может быть null")
    Boolean available;
    Long ownerId;
    ItemRequest request;
}
