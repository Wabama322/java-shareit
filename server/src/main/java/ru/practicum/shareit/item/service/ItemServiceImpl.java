package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingLastAndNextDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDtoResponse addItem(long userId, ItemDtoRequest itemDtoRequest) {
        User user = getUserById(userId);
        Item item = ItemMapper.toItem(itemDtoRequest);
        item.setOwner(user);

        if (itemDtoRequest.getRequestId() != null) {
            ItemRequest request = getRequestById(itemDtoRequest.getRequestId());
            item.setRequest(request);
            request.getItems().add(item);
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDtoResponse updateItem(long userId, long itemId, ItemDtoRequest itemDtoRequest) {
        Item item = getItemById(itemId);
        validateItemOwner(item, userId);

        updateItemFields(item, itemDtoRequest);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemForBookingDto getItemDto(Long ownerId, long itemId) {
        Item item = getItemById(itemId);
        return getItemsWithBookingInfo(List.of(item), ownerId).get(0);
    }

    @Override
    public List<ItemForBookingDto> getAllItemsUser(long userId, int from, int size) {
        validateUserExists(userId);
        Pageable pageable = PageRequest.of(from, size);
        return getItemsWithBookingInfo(
                itemRepository.findAllByOwnerIdOrderById(userId, pageable).getContent(),
                userId
        );
    }

    @Override
    public List<ItemSearchOfTextDto> searchItems(String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(from, size);
        Set<Item> searchResults = new HashSet<>();
        searchResults.addAll(itemRepository.findByNameContainingIgnoreCaseAndAvailableTrue(text, pageable).getContent());
        searchResults.addAll(itemRepository.findByDescriptionContainingIgnoreCaseAndAvailableTrue(text, pageable).getContent());

        return searchResults.stream()
                .map(ItemMapper::toSearchDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDtoResponse addComment(long itemId, long userId, CommentDtoRequest commentDtoRequest) {
        Item item = getItemById(itemId);
        User user = getUserById(userId);

        validateUserCanComment(itemId, userId);

        Comment comment = CommentMapper.toComment(commentDtoRequest, item, user);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);

        return new CommentDtoResponse(
                comment.getId(),
                comment.getText(),
                user.getName(),
                comment.getCreated()
        );
    }

    private void validateItemOwner(Item item, long userId) {
        if (item.getOwner().getId() != userId) {
            throw new AccessDeniedException("У пользователя нет доступа к вещи");
        }
    }

    private void updateItemFields(Item item, ItemDtoRequest dto) {
        Optional.ofNullable(dto.getName())
                .filter(name -> !name.isBlank())
                .ifPresent(item::setName);

        Optional.ofNullable(dto.getDescription())
                .filter(desc -> !desc.isBlank())
                .ifPresent(item::setDescription);

        Optional.ofNullable(dto.getAvailable())
                .ifPresent(item::setAvailable);
    }

    private List<ItemForBookingDto> getItemsWithBookingInfo(List<Item> items, Long userId) {
        Map<Item, List<Comment>> commentsByItem = getCommentsByItems(items);
        Map<Item, List<Booking>> bookingsByItem = getApprovedBookingsByItems(items);

        LocalDateTime now = LocalDateTime.now();
        return items.stream()
                .map(item -> createItemWithBookingInfo(
                        item,
                        userId,
                        commentsByItem.getOrDefault(item, Collections.emptyList()),
                        bookingsByItem.getOrDefault(item, Collections.emptyList()),
                        now
                ))
                .collect(Collectors.toList());
    }

    private Map<Item, List<Comment>> getCommentsByItems(List<Item> items) {
        return commentRepository.findByItemIn(items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));
    }

    private Map<Item, List<Booking>> getApprovedBookingsByItems(List<Item> items) {
        return bookingRepository.findByItemInAndStatus(
                        items, Status.APPROVED, Sort.by(DESC, "start"))
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));
    }

    private ItemForBookingDto createItemWithBookingInfo(Item item, Long userId,
                                                        List<Comment> comments, List<Booking> bookings,
                                                        LocalDateTime now) {

        if (!item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemWithBookings(item, null, null,
                    CommentMapper.toDtoList(comments));
        }

        BookingLastAndNextDto lastBooking = findLastBooking(bookings, now);
        BookingLastAndNextDto nextBooking = findNextBooking(bookings, now);

        return ItemMapper.toItemWithBookings(
                item,
                lastBooking,
                nextBooking,
                CommentMapper.toDtoList(comments)
        );
    }

    private BookingLastAndNextDto findLastBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .findFirst()
                .map(BookingMapper::toItemBookingLastAndNextDto)
                .orElse(null);
    }

    private BookingLastAndNextDto findNextBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toItemBookingLastAndNextDto)
                .orElse(null);
    }

    private void validateUserCanComment(long itemId, long userId) {
        if (!bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, Status.APPROVED, LocalDateTime.now())) {
            throw new BadRequestException("Пользователь не может оставить комментарий к этой вещи");
        }
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private Item getItemById(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    private ItemRequest getRequestById(long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
    }

    private void validateUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }
}