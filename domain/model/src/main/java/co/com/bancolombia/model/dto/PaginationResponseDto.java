package co.com.bancolombia.model.dto;

import java.util.List;

public record PaginationResponseDto<T>(
        String message,
        String code,
        int size,
        long totalElements,
        List<T> data
) {}
