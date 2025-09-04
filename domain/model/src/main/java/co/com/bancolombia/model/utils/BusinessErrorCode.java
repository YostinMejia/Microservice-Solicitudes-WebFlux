package co.com.bancolombia.model.utils;

import lombok.Getter;

@Getter
public enum BusinessErrorCode implements ResponseMessage{
    VALIDATION_FAILED("B400-00", "Create user validation failed"),
    TYPE_LOAN_NOT_FOUND("B404-10", "Type Loan does not exist"),
    USER_NOT_FOUND("B404-00", "User does not exist"),
    UNAUTHORIZED_LOAN_CREATION("B403-00", "User is not allowed to create a loan for another person"),
    INTERNAL_SERVER_ERROR("I500-00", "Internal Server Error");

    private final String businessCode;
    private final String message;

    BusinessErrorCode(String businessCode, String message) {
        this.businessCode = businessCode;
        this.message = message;
    }
}