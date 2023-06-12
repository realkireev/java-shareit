package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.item.cacheservice.ItemCacheService;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static ru.practicum.shareit.common.Variables.USER_HEADER;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemCacheService itemCacheService;

    @PostMapping
    public ResponseEntity<Object> create(
            @Valid @RequestBody ItemRequestDto itemRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemCacheService.create(itemRequestDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestBody ItemRequestDto itemRequestDto,
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @PathVariable Long itemId) {
        return itemCacheService.update(itemRequestDto, itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findByOwnerId(
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemCacheService.findByOwnerId(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(
            @PathVariable Long itemId,
            @RequestHeader(USER_HEADER) @NotNull Long userId) {
        return itemCacheService.findById(itemId, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        return itemCacheService.search(text);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @PathVariable Long itemId) {
        return itemCacheService.delete(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader(USER_HEADER) @NotNull Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemCacheService.addComment(userId, itemId, commentRequestDto);
    }
}
