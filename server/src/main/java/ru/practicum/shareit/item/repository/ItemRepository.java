package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAllByOwnerIdOrderById(Long userId, Pageable pageable);

    List<Item> findByRequestId(Long requestId);

    List<Item> findByRequestIdIn(List<Long> requestIds);

    Page<Item> findByNameContainingIgnoreCaseAndAvailableTrue(String text, Pageable pageable);

    Page<Item> findByDescriptionContainingIgnoreCaseAndAvailableTrue(String text, Pageable pageable);
}
