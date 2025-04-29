package ru.practicum.shareit.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.dto.ItemForItemRequestResponseDto;
import ru.practicum.shareit.user.dto.UserForItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestResponseDto {
    Long id;
    String description;
    UserForItemRequestDto requester;
    LocalDateTime created;
    List<ItemForItemRequestResponseDto> items;
}
