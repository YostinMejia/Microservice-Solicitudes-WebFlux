package co.com.bancolombia.model.exceptions;

import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.model.utils.ResponseMessage;
import lombok.Getter;

import java.util.List;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final List<String> errors;

    /**
     * Constructor for single business exceptions (e.g., user not found).
     *
     * @param businessErrorCode The enum representing the business error.
     */
    public BusinessException(ResponseMessage businessErrorCode) {
        super(businessErrorCode.getMessage());
        this.code = businessErrorCode.getBusinessCode();
        this.errors = null; // No list of errors for a single exception
    }

    /**
     * Constructor for multiple business exceptions (e.g., validation failures).
     *
     * @param errors The list of specific error messages from validation.
     * @param businessErrorCode The enum representing the business error.
     */
    public BusinessException(List<String> errors, ResponseMessage businessErrorCode) {
        super(businessErrorCode.getMessage());
        this.code = businessErrorCode.getBusinessCode();
        this.errors = errors;
    }

    public BusinessException(String message, String code) {
        super(message);
        this.code = code;
        this.errors = null;
    }

}
