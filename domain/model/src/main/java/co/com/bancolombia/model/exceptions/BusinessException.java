package co.com.bancolombia.model.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final List<String> errors;

    public BusinessException(List<String> errors, String message, String code) {
        super(message);
        this.code = code;
        this.errors = errors;
    }
}
