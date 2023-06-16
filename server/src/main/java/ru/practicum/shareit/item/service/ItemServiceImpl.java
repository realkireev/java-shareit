package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.MethodInfo;
import ru.practicum.shareit.item.dto.comment.CommentMapper;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemMapper;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.dto.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.exception.IllegalCommentException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.CommentRepository;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final BookingService bookingService;
    private final RequestService requestService;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final Map<MethodInfo, List<ItemResponseDto>> itemResponseDtoCache = new HashMap<>();
    private final Map<MethodInfo, List<ItemWithBookingsDto>> itemWithBookingsDtoCache = new HashMap<>();

    @Override
    public ItemResponseDto create(Item item, Long ownerId) {
        User owner = userService.findUserById(ownerId);
        item.setOwner(owner);

        Item createdItem = itemRepository.save(item);

        if (item.getRequestId() != null) {
            itemRepository.saveItemBoundWithRequest(createdItem.getId(), item.getRequestId());
            requestService.clearCache();
        }

        itemResponseDtoCache.clear();
        itemWithBookingsDtoCache.clear();
        return ItemMapper.toItemResponseDto(createdItem);
    }

    @Override
    public ItemResponseDto update(Item item, Long itemId, Long ownerId) {
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

        itemResponseDtoCache.clear();
        itemWithBookingsDtoCache.clear();
        return ItemMapper.toItemResponseDto(itemRepository.save(storedItem));
    }

    @Override
    public Item findById(Long itemId) {
        return getItemByIdOrThrowException(itemId);
    }

    @Override
    public List<ItemWithBookingsDto> findByOwnerId(Long ownerId) {
        MethodInfo methodInfo = new MethodInfo("findByOwnerId", ownerId);

        if (itemWithBookingsDtoCache.containsKey(methodInfo)) {
            return itemWithBookingsDtoCache.get(methodInfo);
        }

        List<ItemWithBookingsDto> result;

        result = itemRepository.findByOwnerId(ownerId).stream()
                .sorted(Comparator.comparingLong(Item::getId))
                .map(this::addBookingsToItem)
                .collect(Collectors.toList());

        itemWithBookingsDtoCache.put(methodInfo, result);
        return result;
    }

    @Override
    public List<ItemResponseDto> search(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return Collections.emptyList();
        }

        MethodInfo methodInfo = new MethodInfo("search", searchText);
        if (itemResponseDtoCache.containsKey(methodInfo)) {
            return itemResponseDtoCache.get(methodInfo);
        }

        List<ItemResponseDto> result = itemRepository.searchByNameOrDescriptionIgnoreCaseAndAvailable(
                searchText.toLowerCase())
                .stream()
                .map(ItemMapper::toItemResponseDto)
                .collect(Collectors.toList());

        itemResponseDtoCache.put(methodInfo, result);
        return result;
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item storedItem = getStoredItemAndCheckOwner(itemId, ownerId);
        itemResponseDtoCache.clear();
        itemWithBookingsDtoCache.clear();

        itemRepository.delete(storedItem);
    }

    @Override
    public ItemWithBookingsDto findByIdWithBookings(Long itemId, Long userId) {
        MethodInfo methodInfo = new MethodInfo("findByIdWithBookings", itemId, userId);
        if (itemWithBookingsDtoCache.containsKey(methodInfo)) {
            return itemWithBookingsDtoCache.get(methodInfo).get(0);
        }

        Item item = getItemByIdOrThrowException(itemId);
        ItemWithBookingsDto result;

        if (item.getOwner().getId().equals(userId)) {
            // add booking information for the owner
            result = addBookingsToItem(item);
        } else {
            // don't add booking information for others
            result = ItemMapper.toItemWithBookingsDto(item, null, null);
        }
        itemWithBookingsDtoCache.put(methodInfo, List.of(result));
        return result;
    }

    @Override
    public CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User user = userService.findUserById(userId);
        Item item = getItemByIdOrThrowException(itemId);

        if (!bookingService.hasUserBookedItem(userId, itemId)) {
            throw new IllegalCommentException(String.format("User with id %d has never booked item with id %d ",
                    userId, itemId));
        }

        Comment comment = CommentMapper.toComment(commentRequestDto);
        comment.setUser(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        itemResponseDtoCache.clear();
        itemWithBookingsDtoCache.clear();
        return CommentMapper.toCommentResponseDto(commentRepository.save(comment));
    }

    private Item getItemByIdOrThrowException(Long itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();

            List<Comment> comments = commentRepository.findByItemId(itemId);
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
        User owner = storedItem.getOwner();

        if (owner == null || !Objects.equals(owner.getId(), ownerId)) {
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
