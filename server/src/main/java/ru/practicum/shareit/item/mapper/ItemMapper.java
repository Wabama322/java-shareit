package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingLastAndNextDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {
    public Item toItem(ItemDtoRequest itemDtoRequest) {
        Item item = new Item();
        item.setName(itemDtoRequest.getName());
        item.setDescription(itemDtoRequest.getDescription());
        item.setAvailable(itemDtoRequest.getAvailable());
        return item;
    }

    public ItemDtoResponse toItemDtoResponse(Item item) {
        ItemDtoResponse itemDtoResponse = ItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
        if (item.getRequest() != null) {
            itemDtoResponse.setRequestId(item.getRequest().getId());
        }
        return itemDtoResponse;
    }

    public ItemForBookingDto toItemForBookingDto(Item item, BookingLastAndNextDto lastBooking,
                                                 BookingLastAndNextDto nextBooking, List<CommentDtoResponse> comments) {
        return new ItemForBookingDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                comments);
    }

    public ItemSearchOfTextDto toItemSearchOfTextDto(Item item) {
        return new ItemSearchOfTextDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public ItemForItemRequestResponseDto toItemForItemRequestResponseDto(Item item) {
        ItemForItemRequestResponseDto itemForItemRequestResponseDto = ItemForItemRequestResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
        if (item.getRequest() != null) {
            itemForItemRequestResponseDto.setRequestId(item.getRequest().getId());
        }
        return itemForItemRequestResponseDto;
    }

    public List<ItemForItemRequestResponseDto> toItemForItemRequestsResponseDto(List<Item> item) {
        return item.stream().map(ItemMapper::toItemForItemRequestResponseDto).collect(Collectors.toList());
    }
}
