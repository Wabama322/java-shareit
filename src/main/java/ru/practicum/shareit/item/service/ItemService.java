package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Service

public interface ItemService {
    ItemDto addItem(long userId, ItemDto item);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemDto getItemDto(long itemId);

    List<ItemDto> getAllItemsUser(long userId);

    List<ItemDto> getSearchOfText(String text);
}
