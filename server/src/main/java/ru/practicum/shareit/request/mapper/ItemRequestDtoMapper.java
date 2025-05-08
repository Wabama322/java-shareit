package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserForItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestDtoMapper {
    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        ItemRequest itemRequest = ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        return itemRequest;
    }

    public ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest) {
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requester(new UserForItemRequestDto(itemRequest.getRequester().getId(),
                        itemRequest.getRequester().getName()))
                .created(itemRequest.getCreated())
                .items(List.of())
                .build();
        if (!itemRequest.getItems().isEmpty()) {
            itemRequestResponseDto.setItems(ItemMapper.toItemForItemRequestsResponseDto(itemRequest.getItems()));
        }
        return itemRequestResponseDto;
    }

    public List<ItemRequestResponseDto> toItemRequestsResponseDto(List<ItemRequest> itemRequest) {
        return itemRequest.stream()
                .map(ItemRequestDtoMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }
}
