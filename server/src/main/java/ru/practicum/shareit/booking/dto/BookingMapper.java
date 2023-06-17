package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.item.ItemMapper;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final ItemService itemService;
    private final UserService userService;

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        ItemResponseDto item = ItemMapper.toItemResponseDto(itemService.findById(booking.getItemId()));
        UserResponseDto booker = UserMapper.toUserResponseDto(userService.findUserById(booking.getBookerId()));

        return BookingResponseDto.builder()
                .id(booking.getId())
                .item(item)
                .booker(booker)
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    public Booking toBooking(BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto == null) {
            return null;
        }

        return Booking.builder()
                .itemId(bookingRequestDto.getItemId())
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .build();
    }
}
