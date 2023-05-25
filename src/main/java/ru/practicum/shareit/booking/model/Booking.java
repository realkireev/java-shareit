package ru.practicum.shareit.booking.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;
    private Long bookerId;

    @Column(name = "start_date")
    @FutureOrPresent
    @NotNull
    private LocalDateTime start;

    @Column(name = "end_date")
    @FutureOrPresent
    @NotNull
    private LocalDateTime end;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @AssertTrue(message = "End must be after start")
    private boolean isEndAfterStart() {
        return Objects.nonNull(start) && Objects.nonNull(end) && end.isAfter(start);
    }
}
