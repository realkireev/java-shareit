package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.comment.CommentMapper;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemMapper;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.dto.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemResponseDto create(
            @Valid @RequestBody ItemRequestDto itemRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return ItemMapper.toItemDto(itemService.create(ItemMapper.toItem(itemRequestDto), userId));
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @RequestBody ItemRequestDto itemRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @PathVariable Long itemId) {
        return ItemMapper.toItemDto(itemService.update(ItemMapper.toItem(itemRequestDto), itemId, userId));
    }

    @GetMapping
    public Collection<ItemWithBookingsDto> findByOwnerId(@RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemService.findByOwnerId(userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto findById(
            @PathVariable Long itemId,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemService.findByIdWithBookings(itemId, userId);
    }

    @GetMapping("/search")
    public Collection<ItemResponseDto> search(@RequestParam String text) {
        return itemService.search(text).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @PathVariable Long itemId) {
        itemService.delete(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return CommentMapper.toCommentResponseDto(itemService.addComment(userId, itemId,
                CommentMapper.toComment(commentRequestDto)));
    }
}
