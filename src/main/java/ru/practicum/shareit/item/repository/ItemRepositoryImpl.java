package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemRepositoryImpl implements ItemRepository {
    private Map<Long, Item> items = new HashMap<>();
    private static long id = 1;

    @Override
    public Item addItem(Item item) {
        if (items.containsValue(item)) {
            throw new ObjectNotFoundException("Такой объект уже существует");
        }
        item.setItemId(id++);
        items.put(item.getItemId(), item);
        return item;
    }

    @Override
    public Item updateItem(long itemId, ItemDto itemDto) {
        Item oldItem = getItem(itemId).get();
        if (itemDto.getName() != null) {
            oldItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            oldItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        return oldItem;
    }

    @Override
    public Optional<Item> getItem(long itemId) {
        if (!items.containsKey(itemId)) {
            return Optional.empty();
        }
        return Optional.of(items.get(itemId));
    }

    @Override
    public List<Item> getAllItemsUser(long userId) {
        return items.values().stream().filter(item ->
                item.getOwnerId() == userId).collect(Collectors.toList());
    }

    @Override
    public List<Item> getSearchOfText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(lowerText) ||
                                item.getDescription().toLowerCase().contains(lowerText)))
                .collect(Collectors.toList());
    }

    private List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }
}
