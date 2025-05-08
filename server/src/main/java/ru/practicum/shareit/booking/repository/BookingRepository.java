package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItemInAndStatus(List<Item> items, Status status, Sort sort);

    List<Booking> findByItemId(Long itemId);

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId, Long userId, Status status, LocalDateTime end);

    List<Booking> findByItemIdAndStartLessThanEqualAndEndGreaterThanEqual(
            Long itemId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdOrderByStartDesc(Long userId, Pageable pageable);

    List<Booking> findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeAndStatusOrderByStartDesc(
            Long userId, LocalDateTime end, Status status, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long userId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBeforeAndStatusOrderByStartDesc(
            Long ownerId, LocalDateTime end, Status status, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, Status status, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status, Pageable pageable);

    boolean existsByItemIdAndStatusAndEndLessThanEqualAndBookerId(
            Long itemId,
            Status status,
            LocalDateTime end,
            Long bookerId
    );

    List<Booking> findByBookerIdAndStatusInOrderByStartDesc(
            Long userId,
            List<Status> statuses,
            Pageable pageable
    );

    List<Booking> findByItemOwnerIdAndStatusInOrderByStartDesc(
            Long ownerId,
            List<Status> statuses,
            Pageable pageable);
}
