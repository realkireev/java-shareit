package ru.practicum.shareit.request.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;

@Data
public class RequestRequestDto {
    @NotEmpty
    private String description;
}
