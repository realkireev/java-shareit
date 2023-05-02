package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;
import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {
    Item create(Item item);

    Item update(Item item, Long itemId);

    void delete(Item item);

    Optional<Item> findById(Long itemId);

    Collection<Item> search(String text);

    Collection<Item> findByOwnerId(Long ownerId);
}
