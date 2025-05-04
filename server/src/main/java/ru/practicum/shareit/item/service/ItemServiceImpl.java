package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.dto.BookingLastAndNextDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.AccessDeniedException;
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
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    @Override
    public ItemDtoResponse addItem(long userId, ItemDtoRequest itemDtoRequest) {
        User user = checkUser(userId);
        ItemRequest requester = null;
        Item item = ItemMapper.toItem(itemDtoRequest);
        item.setOwner(user);
        Item addItem = itemRepository.save(item);
        if (itemDtoRequest.getRequestId() != null) {
            requester = checkRequest(itemDtoRequest.getRequestId());
            addItem.setRequest(requester);
            requester.setItems(List.of(addItem));
        }
        return ItemMapper.toItemDtoResponse(addItem);
    }

    @Transactional
    @Override
    public ItemDtoResponse updateItem(long userId, long itemId, ItemDtoRequest itemDtoRequest) {
        Item oldItem = checkItem(itemId);
        long owner = oldItem.getOwner().getId();
        if (userId != owner) {
            throw new AccessDeniedException("У пользователя  нет доступа к вещи");
        }
        if (itemDtoRequest.getName() != null && !itemDtoRequest.getName().isBlank()) {
            oldItem.setName(itemDtoRequest.getName());
        }
        if (itemDtoRequest.getDescription() != null && !itemDtoRequest.getDescription().isBlank()) {
            oldItem.setDescription(itemDtoRequest.getDescription());
        }
        if (itemDtoRequest.getAvailable() != null) {
            oldItem.setAvailable(itemDtoRequest.getAvailable());
        }
        return ItemMapper.toItemDtoResponse(oldItem);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemForBookingDto getItemDto(Long ownerId, long itemId) {
        Item item = checkItem(itemId);
        return fillWithBookingInfo(List.of(item), ownerId).get(0);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemForBookingDto> getAllItemsUser(long userId, int from, int size) {
        checkUser(userId);
        Pageable pageable = PageRequest.of(from, size, Sort.unsorted());
        return fillWithBookingInfo(itemRepository.findAllByOwnerIdOrderById(userId, pageable).getContent(), userId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemSearchOfTextDto> getSearchOfText(String text, int from, int size) {
        if (text.isBlank()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(from, size, Sort.unsorted());
        List<Item> itemList = itemRepository.searchAvailableItemsByNameOrDescription(text, pageable).getContent();
        return itemList.stream().map(ItemMapper::toItemSearchOfTextDto).collect(toList());
    }

    @Transactional
    @Override
    public CommentDtoResponse addComment(long itemId, long userId, CommentDtoRequest commentDtoRequest) {
        Item item = checkItem(itemId);
        User user = checkUser(userId);
        Boolean checkValidate = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId,
                Status.APPROVED, LocalDateTime.now());
        if (!checkValidate) {
            throw new BadRequestException("Неверные параметры");
        }
        Comment comment = CommentMapper.toComment(commentDtoRequest, item, user);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment = commentRepository.save(comment);

        return new CommentDtoResponse(comment.getId(), comment.getText(),
                comment.getAuthor().getName(), comment.getCreated());
    }

    private Item checkItem(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с ID " +
                        itemId + " не зарегистрирован!"));
    }

    private User checkUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с ID " +
                        userId + " не зарегистрирован!"));
    }

    private List<ItemForBookingDto> fillWithBookingInfo(List<Item> items, Long userId) {
        Map<Item, List<Comment>> comments = commentRepository.findByItemIn(
                        items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));
        Map<Item, List<Booking>> bookings = bookingRepository.findByItemInAndStatus(
                        items,  Status.APPROVED, Sort.by(DESC, "start"))
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));
        LocalDateTime now = LocalDateTime.now();
        return items.stream().map(item -> addBookingAndComment(item, userId, comments.getOrDefault(item, List.of()),
                        bookings.getOrDefault(item, List.of()), now))
                .collect(toList());
    }

    public ItemForBookingDto addBookingAndComment(Item item,
                                                  Long userId,
                                                  List<Comment> comments,
                                                  List<Booking> bookings,
                                                  LocalDateTime now) {
        if (item.getOwner().getId().longValue() != userId.longValue()) {
            return ItemMapper.toItemForBookingDto(item, null, null,
                    CommentMapper.toCommentDtoList(comments));
        }
        Booking lastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .findFirst()
                .orElse(null);

        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .reduce((a, b) -> a.getStart().isBefore(b.getStart()) ? a : b)
                .orElse(null);
        BookingLastAndNextDto lastBookingDto = lastBooking != null ?
                BookingMapper.toItemBookingLastAndNextDto(lastBooking) : null;
        BookingLastAndNextDto nextBookingDto = nextBooking != null ?
                BookingMapper.toItemBookingLastAndNextDto(nextBooking) : null;
        return ItemMapper.toItemForBookingDto(item, lastBookingDto, nextBookingDto,
                CommentMapper.toCommentDtoList(comments));
    }

    private ItemRequest checkRequest(Long requestId) {
        return itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос не найден"));
    }
}
