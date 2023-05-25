package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.exception.IllegalCommentException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;

    @Override
    public Item create(Item item, Long ownerId) {
        User owner = userService.findById(ownerId);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Override
    public Item update(Item item, Long itemId, Long ownerId) {
        Item storedItem = getStoredItemAndCheckOwner(itemId, ownerId);

        String name = item.getName();
        String description = item.getDescription();
        Boolean available = item.getAvailable();

        if (name != null) {
            storedItem.setName(name);
        }
        if (description != null) {
            storedItem.setDescription(description);
        }
        if (available != null) {
            storedItem.setAvailable(available);
        }

        return itemRepository.save(storedItem);
    }

    @Override
    public Item findById(Long itemId) {
        return getItemByIdOrThrowException(itemId);
    }

    @Override
    public Collection<ItemWithBookingsDto> findByOwnerId(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(this::addBookingsToItem)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> search(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.searchByNameOrDescriptionIgnoreCaseAndAvailable(searchText.toLowerCase());
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item storedItem = getStoredItemAndCheckOwner(itemId, ownerId);
        itemRepository.delete(storedItem);
    }

    @Override
    public ItemWithBookingsDto findByIdWithBookings(Long itemId, Long userId) {
        Item item = getItemByIdOrThrowException(itemId);
        ItemWithBookingsDto result;

        if (item.getOwner().getId().equals(userId)) {
            // add booking information for the owner
            result = addBookingsToItem(item);
        } else {
            // don't add booking information for others
            result = ItemMapper.toItemWithBookingsDto(item, null, null);
        }

        return result;
    }

    @Override
    public Comment addComment(Long userId, Long itemId, Comment comment) {
        User user = userService.findById(userId);
        Item item = getItemByIdOrThrowException(itemId);

        if (!bookingService.hasUserBookedItem(userId, itemId)) {
            throw new IllegalCommentException(String.format("User with id %d has never booked item with id %d ",
                    userId, itemId));
        }

        comment.setUser(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    private Item getItemByIdOrThrowException(Long itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();

            Collection<Comment> comments = commentRepository.findByItemId(itemId);
            item.setComments(comments);
            return item;
        } else {
            throw new ItemNotFoundException(String.format("Item with id %d not found", itemId));
        }
    }

    private Item getStoredItemAndCheckOwner(Long itemId, Long ownerId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isEmpty()) {
            throw new ItemNotFoundException(String.format("Item with id %s not found", itemId));
        }

        Item storedItem = optionalItem.get();
        if (!Objects.equals(storedItem.getOwner().getId(), ownerId)) {
            throw new WrongOwnerException(String.format("Item with id %s doesn't belong to user with id %s", itemId,
                    ownerId));
        }
        return storedItem;
    }

    private ItemWithBookingsDto addBookingsToItem(Item item) {
        return ItemMapper.toItemWithBookingsDto(item, bookingService.findLastBookingByItemId(item.getId()),
                bookingService.findNextBookingByItemId(item.getId()));
    }

}
