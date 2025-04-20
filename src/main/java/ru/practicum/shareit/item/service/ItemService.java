package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForBookingDto;

import java.util.List;

@Service

public interface ItemService {
    ItemDto addItem(long userId, ItemDto item);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemForBookingDto getItemDto(Long ownerId, long itemId);

    List<ItemForBookingDto> getAllItemsUser(long userId);

    List<ItemDto> getSearchOfText(String text);

    CommentDtoResponse addComment(long itemId, long userId, CommentDtoRequest commentDtoRequest);
}
