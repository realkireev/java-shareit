package ru.practicum.shareit.item.dto.item;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.comment.CommentMapper;
import ru.practicum.shareit.item.model.Item;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemResponseDto toItemDto(Item item) {
        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();

        if (item.getComments() != null) {
            itemResponseDto.setComments(item.getComments().stream()
                    .map(CommentMapper::toCommentResponseDto)
                    .collect(Collectors.toList()));
        }

        return itemResponseDto;
    }

    public static Item toItem(ItemRequestDto itemRequestDto) {
        if (itemRequestDto == null) {
            return null;
        }

        return Item.builder()
                .name(itemRequestDto.getName())
                .description(itemRequestDto.getDescription())
                .available(itemRequestDto.getAvailable())
                .requestId(itemRequestDto.getRequestId())
                .build();
    }

    public static ItemWithBookingsDto toItemWithBookingsDto(Item item, Booking lastBooking, Booking nextBooking) {
        ItemWithBookingsDto result = ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(item.getComments().stream().map(CommentMapper::toCommentResponseDto).collect(Collectors.toList()))
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
