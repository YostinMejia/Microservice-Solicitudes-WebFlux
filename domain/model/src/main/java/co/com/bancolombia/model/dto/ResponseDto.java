package co.com.bancolombia.model.dto;

public record ResponseDto<T>(
        String message,
        String code,
        T data
) {
}
