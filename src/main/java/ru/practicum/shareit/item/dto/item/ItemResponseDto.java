package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import java.util.Collection;

@Data
@Builder
public class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Collection<CommentResponseDto> comments;
    private Long requestId;
}
