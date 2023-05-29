package ru.practicum.shareit.user.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class UserRequestDto {
    @NotNull
    @Email
    private String email;

    @NotNull
    private String name;
}
