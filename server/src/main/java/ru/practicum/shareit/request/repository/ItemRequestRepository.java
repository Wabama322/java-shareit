package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query("select ir from ItemRequest ir " +
            "where ir.requester.id != ?1")
    Page<ItemRequest> findAllByNotRequesterId(Long userId, Pageable pageable);

    Page<ItemRequest> findAll(Pageable pageable);

    @Query
            ("select i from ItemRequest i " +
                    "where i.requester.id = ?1 " +
                    "order by i.created DESC")
    List<ItemRequest> findItemRequestsByUserId(Long userId);
}
