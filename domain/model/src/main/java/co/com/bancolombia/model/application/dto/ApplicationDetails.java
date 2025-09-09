package co.com.bancolombia.model.application.dto;

import java.time.LocalDate;

public record ApplicationDetails(
    String email,
    Long amount,
    LocalDate term,
    String loanType,
    Integer interestRate,
    String state,
    Float applicationMonthPayment
) {
}
