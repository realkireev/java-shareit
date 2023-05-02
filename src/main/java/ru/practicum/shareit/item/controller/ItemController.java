package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
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
    private static final String OWNER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@Valid @RequestBody Item item,
                          @RequestHeader(OWNER_HEADER) @NotNull Long ownerId) {
        return ItemMapper.toItemDto(itemService.create(item, ownerId));
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody Item item,
                          @RequestHeader(OWNER_HEADER) @NotNull Long ownerId,
                          @PathVariable Long itemId) {
        return ItemMapper.toItemDto(itemService.update(item, itemId, ownerId));
    }

    @GetMapping
    public Collection<ItemDto> findByOwnerId(@RequestHeader(OWNER_HEADER) @NotNull Long ownerId) {
        return itemService.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId) {
        return ItemMapper.toItemDto(itemService.findById(itemId));
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam String text) {
        return itemService.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader(OWNER_HEADER) @NotNull Long ownerId, @PathVariable Long itemId) {
        itemService.delete(itemId, ownerId);
    }
}
