package ru.practicum.shareit;

import org.springframework.data.domain.Sort;

public class Variables {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    public static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");
}
