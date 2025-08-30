package co.com.bancolombia.model.dto;

import java.util.List;

public record MultipleErrorsResponseDto(List<String> errors, String message, String code) {
}
