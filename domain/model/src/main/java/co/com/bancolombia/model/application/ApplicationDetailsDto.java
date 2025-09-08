package co.com.bancolombia.model.application;

import java.time.LocalDate;

public record ApplicationDetailsDto(
    String email,
    Long amount,
    LocalDate term,
    String loanType,
    Integer interestRate,
    String state,
    Long applicationMonthPayment
) {
}
