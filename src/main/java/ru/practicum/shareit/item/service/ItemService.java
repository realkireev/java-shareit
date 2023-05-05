package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;
import java.util.Collection;

public interface ItemService {
    Item create(Item item, Long ownerId);

    Item update(Item item, Long itemId, Long ownerId);

    Item findById(Long itemId);

    Collection<Item> findByOwnerId(Long ownerId);

    Collection<Item> search(String text);

    void delete(Long itemId, Long ownerId);
}
