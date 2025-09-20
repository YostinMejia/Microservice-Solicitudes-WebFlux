package co.com.bancolombia.api.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DebtCapacityDto(
        @NotNull
        @Positive
        double totalIncome,
        @NotNull
        @Positive
        double currentMonthlyDebt,
        @NotNull
        @Positive
        double interestRate,
        @NotNull
        @Positive
        int termMonths,
        @NotNull
        @Positive
        float loanAmount,
        @NotNull(message = "idApplication: should not be empty and UUID format")
        UUID idApplication
        ) {
}
