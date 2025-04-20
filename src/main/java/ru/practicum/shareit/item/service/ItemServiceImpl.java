package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForBookingDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
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

    @Transactional
    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        User user = checkUser(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        Item addItem = itemRepository.save(item);
        return ItemMapper.toItemDto(addItem);
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        Item oldItem = checkItem(itemId);
        long owner = oldItem.getOwner().getId();
        if (userId != owner) {
            throw new AccessDeniedException("У пользователя нет доступа к вещи");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            oldItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            oldItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(oldItem);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemForBookingDto getItemDto(Long ownerId, long itemId) {
        Item item = checkItem(itemId);
        return fillWithBookingInfo(List.of(item), ownerId).get(0);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemForBookingDto> getAllItemsUser(long userId) {
        checkUser(userId);
        return fillWithBookingInfo(itemRepository.findAllByOwnerIdOrderById(userId), userId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getSearchOfText(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        List<Item> itemList = getSearch(text);
        return itemList.stream().map(ItemMapper::toItemDto).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<Item> getSearch(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.findByNameOrDescription(text);
    }

    @Transactional
    @Override
    public CommentDtoResponse addComment(long itemId, long userId, CommentDtoRequest commentDtoRequest) {
        Item item = checkItem(itemId);
        User user = checkUser(userId);

        boolean isValid = bookingRepository.existsValidBooking(
                itemId,
                Status.APPROVED,
                LocalDateTime.now(),
                userId
        );

        if (!isValid) {
            throw new BadRequestException("Нельзя оставить комментарий: бронирование не найдено или не завершено");
        }

        Comment comment = CommentMapper.toComment(commentDtoRequest, item, user);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDtoResponse(commentRepository.save(comment));
    }

    private Item checkItem(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id " +
                        itemId + " не зарегистрирована"));
    }

    private User checkUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " +
                        userId + " не зарегистрирован"));
    }

    private List<ItemForBookingDto> fillWithBookingInfo(List<Item> items, Long userId) {
        Map<Item, List<Comment>> comments = commentRepository.findByItemIn(
                        items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));
        Map<Item, List<Booking>> bookings = bookingRepository.findByItemInAndStatus(
                        items, Status.APPROVED, Sort.by(DESC, "start"))
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));
        LocalDateTime now = LocalDateTime.now();
        return items.stream().map(item -> addBookingAndComment(item, userId, comments.getOrDefault(item, List.of()),
                        bookings.getOrDefault(item, List.of()), now))
                .collect(toList());
    }

    private ItemForBookingDto addBookingAndComment(Item item,
                                                   Long userId,
                                                   List<Comment> comments,
                                                   List<Booking> bookings,
                                                   LocalDateTime now) {
        if (item.getOwner().getId().longValue() != userId.longValue()) {
            return ItemMapper.toItemForBookingMapper(item, null, null,
                    CommentMapper.commentDtoList(comments));
        }
        Booking lastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .findFirst()
                .orElse(null);

        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .reduce((a, b) -> a.getStart().isBefore(b.getStart()) ? a : b)
                .orElse(null);
        BookingForItemDto lastBookingDto = lastBooking != null ?
                BookingMapper.toItemBookingInfoDto(lastBooking) : null;
        BookingForItemDto nextBookingDto = nextBooking != null ?
                BookingMapper.toItemBookingInfoDto(nextBooking) : null;
        return ItemMapper.toItemForBookingMapper(item, lastBookingDto, nextBookingDto,
                CommentMapper.commentDtoList(comments));
    }
}
