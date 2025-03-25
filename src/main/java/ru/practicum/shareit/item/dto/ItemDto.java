package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    String name;

    @NotBlank(message = "Описание не может быть пустым")
    String description;

    @NotNull(message = "Статус доступности обязателен")
    Boolean available;

    Long ownerId;
    Long requestId;
}
