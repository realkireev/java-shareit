package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;

import java.util.List;

@Getter
@Setter
@Builder
public class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private List<CommentResponseDto> comments;
    private Long requestId;
}
