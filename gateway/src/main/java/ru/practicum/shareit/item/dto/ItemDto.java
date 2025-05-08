package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;
    @NotBlank(message = "Поле не может быть пустым")
    String name;
    @NotBlank(message = "Поле не может быть пустым")
    String description;
    @NotNull(message = "Поле не может быть null")
    Boolean available;
    Long ownerId;
    Long request;
}
