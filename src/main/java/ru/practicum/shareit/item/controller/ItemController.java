package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemController {
    private final ItemService itemService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@Valid @RequestBody Item item, @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return ItemMapper.toItemDto(itemService.create(item, userId));
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody Item item, @RequestHeader(USER_HEADER) @NotNull Long userId,
                          @PathVariable Long itemId) {
        return ItemMapper.toItemDto(itemService.update(item, itemId, userId));
    }

    @GetMapping
    public Collection<ItemWithBookingsDto> findByOwnerId(@RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemService.findByOwnerId(userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto findById(@PathVariable Long itemId, @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemService.findByIdWithBookings(itemId, userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam String text) {
        return itemService.search(text).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader(USER_HEADER) @NotNull Long userId, @PathVariable Long itemId) {
        itemService.delete(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_HEADER) @NotNull Long userId, @PathVariable Long itemId,
                                 @RequestBody @Valid Comment comment) {
        return CommentMapper.toCommentDto(itemService.addComment(userId, itemId, comment));
    }
}
