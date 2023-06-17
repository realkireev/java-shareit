package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.dto.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemResponseDto create(Item item, Long ownerId);

    ItemResponseDto update(Item item, Long itemId, Long ownerId);

    Item findById(Long itemId);

    CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto comment);

    ItemWithBookingsDto findByIdWithBookings(Long itemId, Long userId);

    List<ItemWithBookingsDto> findByOwnerId(Long ownerId);

    List<ItemResponseDto> search(String text);

    void delete(Long itemId, Long ownerId);
}
