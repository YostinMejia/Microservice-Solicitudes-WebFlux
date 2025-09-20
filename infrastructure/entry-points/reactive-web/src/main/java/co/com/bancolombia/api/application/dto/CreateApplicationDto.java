package co.com.bancolombia.api.application.dto;

import jakarta.validation.constraints.*;

public record CreateApplicationDto(
        @NotNull
        @Min(1)
        Integer amount,
        @NotNull
        @Pattern(regexp = "^[1-3]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"
                , message = "Term should have the format yyyy-MM-dd and be a valid date")
        String term,

        @NotNull
        @NotBlank
        String typeLoanName,
        @NotNull
        @NotBlank
        String document,
        @NotNull
        @NotBlank
        String email


) {
}
