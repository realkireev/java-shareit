package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import java.util.Collection;

public interface ItemService {
    Item create(Item item, Long ownerId);

    Item update(Item item, Long itemId, Long ownerId);

    Item findById(Long itemId);

    Comment addComment(Long userId, Long itemId, Comment comment);

    ItemWithBookingsDto findByIdWithBookings(Long itemId, Long userId);

    Collection<ItemWithBookingsDto> findByOwnerId(Long ownerId);

    Collection<Item> search(String text);

    void delete(Long itemId, Long ownerId);
}
