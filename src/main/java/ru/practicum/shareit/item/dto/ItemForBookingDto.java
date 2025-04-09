package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingForItemDto;

import java.util.List;

@Data
@AllArgsConstructor
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemForBookingDto {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingForItemDto lastBooking;
    BookingForItemDto nextBooking;
    final List<CommentDtoResponse> comments;
}
