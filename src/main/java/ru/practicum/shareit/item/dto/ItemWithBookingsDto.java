package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Collection;

@Data
@Builder
public class ItemWithBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingInfo lastBooking;
    private BookingInfo nextBooking;
    private Collection<CommentDto> comments;
}

@Data
@Builder
class BookingInfo {
    private final Long id;
    private final Long bookerId;
}
