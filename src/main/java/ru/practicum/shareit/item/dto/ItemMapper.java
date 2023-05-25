package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(item.getComments().stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()))
                .build();
    }

    public static ItemWithBookingsDto toItemWithBookingsDto(Item item, Booking lastBooking, Booking nextBooking) {
        ItemWithBookingsDto result = ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(item.getComments().stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()))
                .build();

        if (lastBooking != null) {
            result.setLastBooking(new BookingInfo(lastBooking.getId(), lastBooking.getBookerId()));
        }
        if (nextBooking != null) {
            result.setNextBooking(new BookingInfo(nextBooking.getId(), nextBooking.getBookerId()));
        }

        return result;
    }
}
