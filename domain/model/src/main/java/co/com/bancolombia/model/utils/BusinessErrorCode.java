package co.com.bancolombia.model.utils;

import lombok.Getter;

@Getter
public enum BusinessErrorCode implements ResponseMessage{
    VALIDATION_FAILED("B400-00", "Validation failed"),
    APPLICATION_LOAN_NOT_FOUND("B404-10", "Application Loan does not exist"),
    TYPE_LOAN_NOT_FOUND("B404-10", "Type Loan does not exist"),
    STATE_NOT_FOUND("B404-10", "state does not exist"),
    USER_NOT_FOUND("B404-00", "User does not exist"),
    UNAUTHORIZED_LOAN_CREATION("B403-00", "User is not allowed to create a loan for another person"),
    UNAUTHORIZED_UPDATE_STATE("B403-00", "User is not allowed to update an application state"),
    UNAUTHORIZED_GET_LOAN_TYPE("B403-00", "User is not allowed to get loan types"),
    INTERNAL_SERVER_ERROR("I500-00", "Internal Server Error");

    private final String businessCode;
    private final String message;

    BusinessErrorCode(String businessCode, String message) {
        this.businessCode = businessCode;
        this.message = message;
    }
}