package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;
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
            Long itemId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdAndEndBeforeAndStatusOrderByStartDesc(
            Long userId, LocalDateTime end, Status status);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long userId, LocalDateTime start);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndEndBeforeAndStatusOrderByStartDesc(
            Long ownerId, LocalDateTime end, Status status);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = 'WAITING' order by b.start DESC")
    List<Booking> findWaitingBookingsByBooker(Long userId);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = 'REJECTED' order by b.start DESC")
    List<Booking> findRejectedBookingsByBooker(Long userId);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = 'WAITING' order by b.start DESC")
    List<Booking> findWaitingBookingsByOwner(Long ownerId);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = 'REJECTED' order by b.start DESC")
    List<Booking> findRejectedBookingsByOwner(Long ownerId);

    @Query("""
        select new java.lang.Boolean(COUNT(b) > 0)
        from Booking b
        where (b.item.id = ?1 and b.status = ?2 and b.end = ?3 or b.end < ?3)
        and b.booker.id = ?4
    """)
    Boolean existsValidBooking(Long itemId, Status status, LocalDateTime end, Long userId);

    @Query("""
    select b from Booking b
    where b.booker.id = :userId
    and b.status in :statuses
    order by b.start DESC
""")
    List<Booking> findByBookerIdAndStatusIn(
            @Param("userId") Long userId,
            @Param("statuses") List<Status> statuses);

    @Query("""
        select b from Booking b
        where b.item.owner.id = ?1
        and (b.status = ?2 or b.status = ?3)
        order by b.start DESC
    """)
    List<Booking> findByOwnerAndStatusIn(Long ownerId, Status status1, Status status2);
}