package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectForbiddenException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemDto addItem(long userId, ItemDto item) {
        User user = userRepository.getUser(userId).orElseThrow(() ->
                new ObjectNotFoundException("Пользователь с ID " +
                        userId + " не зарегистрирован!"));
        item.setOwnerId(user.getId());
        Item addItem = itemRepository.addItem(ItemMapper.toItem(item,userId));
        return ItemMapper.toItemDto(addItem);
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        Item oldItem = getItem(itemRepository.getItem(itemId).get().getItemId());
        long owner = oldItem.getOwnerId();
        if (userId != owner) {
            throw new ObjectForbiddenException("У пользователя с ID {} нет доступа к вещи");
        }
        Item updateItem = itemRepository.updateItem(itemId, itemDto);
        return ItemMapper.toItemDto(updateItem);
    }

    @Override
    public ItemDto getItemDto(long itemId) {
        return ItemMapper.toItemDto(getItem(itemId));
    }

    private Item getItem(long itemId) {
        return itemRepository.getItem(itemId).orElseThrow(() ->
                new ObjectNotFoundException("Вещь с ID " +
                        itemId + " не зарегистрирована!"));
    }

    @Override
    public List<ItemDto> getAllItemsUser(long userId) {
        return itemRepository.getAllItemsUser(userId).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getSearchOfText(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        List<Item> itemList = itemRepository.getSearchOfText(text);
        return itemList.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}
