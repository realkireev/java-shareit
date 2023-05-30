package ru.practicum.shareit.request.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class RequestRequestDto {
    @NotNull
    private String description;
}
