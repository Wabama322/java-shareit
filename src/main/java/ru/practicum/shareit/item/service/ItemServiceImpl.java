package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        log.info("Добавление вещи для пользователя ID: {}", userId);

        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(false);
            log.debug("Установлено значение available по умолчанию: false");
        }

        Item newItem = ItemMapper.toItem(itemDto, userId);
        Item savedItem = itemRepository.addItem(newItem);

        log.info("Вещь успешно добавлена с ID: {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        log.info("Обновление вещи ID: {} пользователем ID: {}", itemId, userId);

        Item existingItem = itemRepository.getItem(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        if (existingItem.getOwnerId() != userId) {
            throw new AccessDeniedException("У пользователя с ID " + userId + " нет прав для изменения этой вещи");
        }

        Item updatedItem = itemRepository.updateItem(itemId, itemDto);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemDto(long itemId) {
        log.info("Получение вещи ID: {}", itemId);
        return itemRepository.getItem(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    public List<ItemDto> getAllItemsUser(long userId) {
        log.info("Получение всех вещей пользователя ID: {}", userId);
        return itemRepository.getAllItemsUser(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getSearchOfText(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        return itemRepository.getSearchOfText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}