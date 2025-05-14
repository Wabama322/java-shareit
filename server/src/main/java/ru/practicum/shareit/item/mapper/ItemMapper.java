package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingLastAndNextDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class ItemMapper {

    public Item toItem(ItemDtoRequest dto) {
        Objects.requireNonNull(dto, "ItemDtoRequest не может быть null");

        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .build();
    }

    public ItemDtoResponse toItemDto(Item item) {
        if (item == null) return null;

        ItemDtoResponse.ItemDtoResponseBuilder builder = ItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable());

        if (item.getRequest() != null) {
            builder.requestId(item.getRequest().getId());
        }

        return builder.build();
    }

    public ItemForBookingDto toItemWithBookings(Item item,
                                                BookingLastAndNextDto lastBooking,
                                                BookingLastAndNextDto nextBooking,
                                                List<CommentDtoResponse> comments) {
        if (item == null) return null;

        return ItemForBookingDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments != null ? comments : List.of())
                .build();
    }

    public ItemSearchOfTextDto toSearchDto(Item item) {
        if (item == null) return null;

        return ItemSearchOfTextDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public ItemForItemRequestResponseDto toRequestItemDto(Item item) {
        if (item == null) return null;

        ItemForItemRequestResponseDto.ItemForItemRequestResponseDtoBuilder builder =
                ItemForItemRequestResponseDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable());

        if (item.getRequest() != null) {
            builder.requestId(item.getRequest().getId());
        }

        return builder.build();
    }

    public List<ItemForItemRequestResponseDto> toRequestItemDtoList(List<Item> items) {
        if (items == null) return List.of();

        return items.stream()
                .filter(Objects::nonNull)
                .map(ItemMapper::toRequestItemDto)
                .filter(Objects::nonNull)
                .toList();
    }
}