package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item addItem(Item item);

    Item updateItem(long itemId, ItemDto itemDto);

    Optional<Item> getItem(long itemId);

    List<Item> getAllItemsUser(long userId);

    List<Item> getSearchOfText(String text);
}
