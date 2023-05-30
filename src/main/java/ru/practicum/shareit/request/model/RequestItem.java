package ru.practicum.shareit.request.model;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RequestItem {
    @Id
    private Long id;
    private Long requestId;
    private Long itemId;
}
