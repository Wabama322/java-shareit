package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingForResponse {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    ItemWithBookingDto item;
    UserWithIdAndNameDto booker;
    Status status;
}
