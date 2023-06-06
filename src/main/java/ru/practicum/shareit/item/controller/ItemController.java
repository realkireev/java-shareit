package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemMapper;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.dto.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static ru.practicum.shareit.Variables.USER_HEADER;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto create(
            @Valid @RequestBody ItemRequestDto itemRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemService.create(ItemMapper.toItem(itemRequestDto), userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @RequestBody ItemRequestDto itemRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @PathVariable Long itemId) {
        return itemService.update(ItemMapper.toItem(itemRequestDto), itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingsDto> findByOwnerId(
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemService.findByOwnerId(userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto findById(
            @PathVariable Long itemId,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemService.findByIdWithBookings(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(@RequestParam String text) {
        return itemService.search(text);
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
        return itemService.addComment(userId, itemId, commentRequestDto);
    }
}
