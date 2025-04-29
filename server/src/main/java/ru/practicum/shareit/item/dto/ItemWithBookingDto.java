package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ItemWithBookingDto {
    private Long id;
    private String name;
}
