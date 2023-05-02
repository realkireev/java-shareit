package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public Item create(Item item, Long ownerId) {
        User owner = userService.findById(ownerId);
        item.setOwner(owner);
        return itemStorage.create(item);
    }

    @Override
    public Item update(Item item, Long itemId, Long ownerId) {
        Item storedItem = getStoredItemAndCheckOwner(itemId, ownerId);

        String name = item.getName();
        String description = item.getDescription();
        Boolean available = item.getAvailable();

        if (name != null) {
            storedItem.setName(item.getName());
        }
        if (description != null) {
            storedItem.setDescription(item.getDescription());
        }
        if (available != null) {
            storedItem.setAvailable(item.getAvailable());
        }

        return itemStorage.update(storedItem, itemId);
    }

    @Override
    public Item findById(Long itemId) {
        Optional<Item> item = itemStorage.findById(itemId);
        if (item.isEmpty()) {
            throw new ItemNotFoundException(String.format("Item with id %s not found", itemId));
        }
        return item.get();
    }

    @Override
    public Collection<Item> findByOwnerId(Long ownerId) {
        return itemStorage.findByOwnerId(ownerId);
    }

    @Override
    public Collection<Item> search(String text) {
        return itemStorage.search(text.toLowerCase());
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item storedItem = getStoredItemAndCheckOwner(itemId, ownerId);
        itemStorage.delete(storedItem);
    }

    private Item getStoredItemAndCheckOwner(Long itemId, Long ownerId) {
        Optional<Item> optionalItem = itemStorage.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new ItemNotFoundException(String.format("Item with id %s not found", itemId));
        }

        Item storedItem = optionalItem.get();
        if (!Objects.equals(storedItem.getOwner().getId(), ownerId)) {
            throw new WrongOwnerException(
                    String.format("Item with id %s doesn't belong to user with id %s", itemId, ownerId)
            );
        }
        return storedItem;
    }
}
