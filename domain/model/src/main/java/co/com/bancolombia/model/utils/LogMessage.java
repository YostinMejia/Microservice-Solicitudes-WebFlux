package co.com.bancolombia.model.utils;

import lombok.Getter;

@Getter
public enum LogMessage {
    SAVE_USER_CALLED("Save user called"),
    EXIST_BY_DOCUMENT_CALLED("Exist by Document called"),
    GET_ALL_CALLED("Get all called");

    private final String message;

    LogMessage(String message) {
        this.message = message;
    }
}
