package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
public class BookingRequestDto {
    @NotNull
    private Long itemId;

    @FutureOrPresent
    private LocalDateTime start;

    @FutureOrPresent
    private LocalDateTime end;

    @AssertTrue(message = "End must be after start")
    private boolean isEndAfterStart() {
        return Objects.nonNull(start) && Objects.nonNull(end) && end.isAfter(start);
    }
}
