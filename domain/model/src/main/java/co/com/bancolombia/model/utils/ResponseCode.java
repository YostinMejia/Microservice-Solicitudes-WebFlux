package co.com.bancolombia.model.utils;

import lombok.Getter;

@Getter
public enum ResponseCode implements ResponseMessage {
    APPLICATION_CREATED("201-00", "Application created successfully");

    private final String businessCode;
    private final String message;

    ResponseCode(String businessCode, String message) {
        this.businessCode = businessCode;
        this.message = message;
    }
}