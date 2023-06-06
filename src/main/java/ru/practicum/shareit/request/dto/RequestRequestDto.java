package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class RequestRequestDto {
    @NotEmpty
    private String description;
}
