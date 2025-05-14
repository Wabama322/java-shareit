package ru.practicum.shareit.booking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithIdAndNameDto {
    private Long id;
    private String name;
}
