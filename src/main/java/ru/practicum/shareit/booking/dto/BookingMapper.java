package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.service.UserService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingMapper {
    private final ItemService itemService;
    private final UserService userService;

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        ItemDto item = ItemMapper.toItemDto(itemService.findById(booking.getItemId()));
        UserDto booker = UserMapper.toUserDto(userService.findById(booking.getBookerId()));

        return BookingDto.builder()
                .id(booking.getId())
                .item(item)
                .booker(booker)
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }
}
