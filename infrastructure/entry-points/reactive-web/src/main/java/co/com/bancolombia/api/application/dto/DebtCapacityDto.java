package co.com.bancolombia.api.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DebtCapacityDto(
        @NotNull
        @Positive
        double interestRate,
        @NotNull
        @Positive
        int termMonths,
        @NotNull
        @Positive
        float loanAmount,
        @NotNull
        UUID idApplication,
        @NotNull
        @Email
        String email
        ) {
}
