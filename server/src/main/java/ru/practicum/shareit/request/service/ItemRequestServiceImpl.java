package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestDtoMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public ItemRequestResponseDto addItemRequest(long userId, ItemRequestDto itemRequestDto) {
        User user = checkUser(userId);
        ItemRequest itemRequest = ItemRequestDtoMapper.toItemRequest(itemRequestDto, user);
        ItemRequest savedRequest = requestRepository.save(itemRequest);
        savedRequest.setItems(Collections.emptyList());
        return ItemRequestDtoMapper.toItemRequestResponseDto(savedRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestResponseDto> getItemRequestsByUserId(long userId) {
        checkUser(userId);
        List<ItemRequest> itemRequests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        return enrichRequestsWithItems(itemRequests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllItemRequests(long userId, int from, int size) {
        checkUserExists(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        Page<ItemRequest> itemRequestsPage = requestRepository.findByRequesterIdNot(userId, pageable);
        return enrichRequestsWithItems(itemRequestsPage.getContent());
    }

    @Override
    public ItemRequestResponseDto getItemRequest(long requestId, long userId) {
        checkUser(userId);
        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос c ID " + requestId + " не найден"));
        List<Item> items = itemRepository.findByRequestId(requestId);
        itemRequest.setItems(items);
        return ItemRequestDtoMapper.toItemRequestResponseDto(itemRequest);
    }

    private List<ItemRequestResponseDto> enrichRequestsWithItems(List<ItemRequest> itemRequests) {
        if (itemRequests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequest = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        itemRequests.forEach(request ->
                request.setItems(itemsByRequest.getOrDefault(request.getId(), Collections.emptyList()))
        );

        return ItemRequestDtoMapper.toItemRequestsResponseDto(itemRequests);
    }

    private User checkUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private void checkUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не зарегистрирован!");
        }
    }
}