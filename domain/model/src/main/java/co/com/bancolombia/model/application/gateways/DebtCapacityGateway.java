package co.com.bancolombia.model.application.gateways;

import reactor.core.publisher.Mono;

public interface DebtCapacityGateway {
    Mono<String> loanDecision(double totalIncome, double currentMonthlyDebt, double interestRate, int termMonths, float loanAmount );
}
