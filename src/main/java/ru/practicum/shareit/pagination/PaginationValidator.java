package ru.practicum.shareit.pagination;

public class PaginationValidator {
    public static void validate(int from, int size) {
        if (from < 0) {
            throw new InvalidPaginationException("Parameter 'from' must be greater than or equal to 0");
        }

        if (size < 1) {
            throw new InvalidPaginationException("Parameter 'size' must be greater than 0");
        }
    }
}
