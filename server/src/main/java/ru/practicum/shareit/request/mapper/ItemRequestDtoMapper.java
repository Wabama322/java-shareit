package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserForItemRequestDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestDtoMapper {
    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requester(user)
                .created(LocalDateTime.now())
                .build();
    }

    public ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest) {
        if (itemRequest == null || itemRequest.getRequester() == null) {
            throw new IllegalStateException("ItemRequest or requester is null");
        }

        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requester(new UserForItemRequestDto(
                        itemRequest.getRequester().getId(),
                        itemRequest.getRequester().getName()))
                .created(itemRequest.getCreated())
                .items(itemRequest.getItems() != null ?
                        itemRequest.getItems().stream()
                                .map(ItemMapper::toRequestItemDto)
                                .toList() :
                        List.of())
                .build();
    }

    public List<ItemRequestResponseDto> toItemRequestsResponseDto(List<ItemRequest> itemRequest) {
        return itemRequest.stream()
                .map(ItemRequestDtoMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }
}