package co.com.bancolombia.api.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DebtCapacityDto (
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
        float loanAmount
){
}
