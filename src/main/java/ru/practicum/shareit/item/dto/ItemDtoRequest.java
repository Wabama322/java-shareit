package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoRequest {
    Long id;
    @NotBlank(message = "Поле не может быть пустым")
    String name;
    @NotBlank(message = "Поле не может быть пустым")
    String description;
    @NotBlank(message = "Поле не может быть пустым")
    Boolean available;
}
