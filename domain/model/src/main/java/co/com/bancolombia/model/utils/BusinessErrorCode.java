package co.com.bancolombia.model.utils;

import lombok.Getter;

@Getter
public enum BusinessErrorCode {
    VALIDATION_FAILED("B400-00", "Create user validation failed"),
    USER_NOT_FOUND("B404-00", "User does not exist"),
    TYPE_LOAN_NOT_FOUND("B404-10", "Type Loan does not exist"),
    USER_ALREADY_REGISTERED("B400-01", "User registered already"),
    ROL_NOT_EXIST("B400-02", "Rol does not exist"),
    INTERNAL_SERVER_ERROR("I500-00", "Internal Server Error"),
    INVALID_CREDENTIALS("B401-00", "Invalid credentials");


    private final String businessCode;
    private final String message;

    BusinessErrorCode(String businessCode, String message) {
        this.businessCode = businessCode;
        this.message = message;
    }
}