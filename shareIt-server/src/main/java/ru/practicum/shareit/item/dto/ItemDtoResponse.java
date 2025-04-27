package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoResponse {
    Long id;
    String name;
    String description;
    Boolean available;
    Long requestId;
}
