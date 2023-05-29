package ru.practicum.shareit.item.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;
import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Collection<Item> findByOwnerId(Long ownerId);

    @Query("SELECT i FROM Item i WHERE LOWER(CONCAT(i.name, i.description)) LIKE %:searchText% AND i.available = true")
    Collection<Item> searchByNameOrDescriptionIgnoreCaseAndAvailable(@Param("searchText") String searchText);
}
