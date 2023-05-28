package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class BookingInfo {
    private final Long id;
    private final Long bookerId;
}
