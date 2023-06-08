package ru.practicum.shareit.item.dto.comment;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class CommentRequestDto {
    @NotEmpty
    private String text;
}
