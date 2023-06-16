package ru.practicum.shareit.common;

import org.springframework.data.domain.Sort;

public class Variables {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    public static final String CONTENT_TYPE = "application/json";
    public static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start")
            .and(Sort.by(Sort.Direction.ASC, "id"));
}
