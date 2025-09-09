package co.com.bancolombia.model.dto;

import java.util.List;

public record MultipleErrorsResponse(List<String> errors, String message, String code) {
}
