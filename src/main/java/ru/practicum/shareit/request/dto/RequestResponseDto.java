package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import java.time.LocalDateTime;
import java.util.Collection;

@Data
@Builder
public class RequestResponseDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private Collection<ItemResponseDto> items;
}
