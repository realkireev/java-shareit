package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemMemoryStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long currentId = 1L;

    @Override
    public Item create(Item item) {
        item.setId(currentId++);
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item, Long itemId) {
        return items.put(itemId, item);
    }

    @Override
    public void delete(Item item) {
        items.remove(item.getId());
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        if (!items.containsKey(itemId)) {
            return Optional.empty();
        }
        return Optional.of(items.get(itemId));
    }

    @Override
    public Collection<Item> search(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        return items.values().stream()
                .filter(x -> x.getName()
                        .concat(x.getDescription())
                        .toLowerCase()
                        .contains(text))
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(x -> x.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }
}
