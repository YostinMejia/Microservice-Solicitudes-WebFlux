package co.com.bancolombia.model.utils;

import lombok.Getter;

@Getter
public enum ResponseCode {
    APPLICATION_CREATED("201-00", "Application created successfully");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}