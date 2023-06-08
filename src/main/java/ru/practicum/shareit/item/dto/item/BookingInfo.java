package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
class BookingInfo {
    private final Long id;
    private final Long bookerId;
}
