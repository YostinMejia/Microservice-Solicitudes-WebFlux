package co.com.bancolombia.model.dto;

public record Response<T>(
        String message,
        String code,
        T data
) {
}
