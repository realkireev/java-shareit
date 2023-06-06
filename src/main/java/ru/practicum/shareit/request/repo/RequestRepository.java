package ru.practicum.shareit.request.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Page<Request> findAllByUserIdNot(Long userId, Pageable pageable);

    @Query("SELECT r FROM Request r WHERE r.userId = :userId ")
    List<Request> findByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT i FROM RequestItem ri, Item i WHERE ri.itemId = i.id AND ri.requestId = :requestId")
    List<Item> findItemsByRequestId(@Param("requestId") Long requestId);
}
