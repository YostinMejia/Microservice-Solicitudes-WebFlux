package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationDetails;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.DebtCapacityGateway;
import co.com.bancolombia.model.auth.Role;
import co.com.bancolombia.model.auth.gateway.AuthGateway;
import co.com.bancolombia.model.dto.PaginationParams;
import co.com.bancolombia.model.dto.PaginationResponse;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateNotificationGateway;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
import co.com.bancolombia.model.user.UserGateway;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.model.utils.DefaultProperties;
import co.com.bancolombia.usecase.state.StateUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@RequiredArgsConstructor
public class ApplicationUseCase {
    private final ApplicationRepository applicationRepository;
    private final TypeLoanRepository typeLoanRepository;
    private final StateRepository stateRepository;
    private final UserGateway userGateway;
    private final AuthGateway authGateway;
    private final StateUseCase stateUseCase;
    private final StateNotificationGateway stateNotificationGateway;
    private final DebtCapacityGateway debtCapacityGateway;

    public Mono<Application> save(Application application, String typeLoanName, String userDocument, String userEmail, String authHeader) {
        final State initialState = new State().toBuilder().name(DefaultProperties.INITIAL_STATE_NAME.getProperty()).build();

        return
                authGateway.isSameEmailAsToken(userEmail, authHeader)
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.UNAUTHORIZED_LOAN_CREATION)))
                        .flatMap(exists -> userGateway.existByDocumentAndEmail(userDocument, userEmail, authHeader))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.USER_NOT_FOUND)))
                        .flatMap(exists -> typeLoanRepository.findByName(typeLoanName))
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.TYPE_LOAN_NOT_FOUND)))
                        .zipWhen(typeLoan -> {
                            Application appWithLoanId = application.toBuilder().idTypeLoan(typeLoan.getId()).build();
                            return stateRepository.save(initialState).map(stateSaved ->
                                            appWithLoanId.toBuilder().idState(stateSaved.getId()).build())
                                    .flatMap(applicationRepository::save);
                        })
                        .filter(tuple -> tuple.getT1().getAutomaticValidation())
                        .flatMap(tuple -> calculateDebtCapacity(
                                tuple.getT1().getInterestRate(),
                                (int) Math.ceil(ChronoUnit.DAYS.between(LocalDate.now(), tuple.getT2().getTerm()) / 30.),
                                tuple.getT2().getAmount(),
                                tuple.getT2().getId(),
                                tuple.getT2().getEmail())
                                .thenReturn(tuple.getT2()));
    }


    public Mono<PaginationResponse<ApplicationDetails>> findByFilter(ApplicationFilter applicationFilter, PaginationParams paginationParams, String authHeader) {
        return authGateway.getRolByAuthHeaderToken(authHeader)
                .filter(email -> !email.isEmpty() || !email.equals(Role.ADVISOR.getValue()))
                .flatMap(x -> applicationRepository.findByFilter(applicationFilter, paginationParams))
                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.UNAUTHORIZED_GET_LOAN_TYPE)));
    }

    public Mono<Application> update(UUID idApplication, String state, String authHeader) {
        return authGateway.getRolByAuthHeaderToken(authHeader)
                .filter(role -> !role.isEmpty() || !role.equals(Role.ADVISOR.getValue()))
                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.UNAUTHORIZED_UPDATE_STATE)))
                .then(Mono.defer(() -> updateApplicationState(idApplication,state)));
    }

    public Mono<Application> updateApplicationState(UUID idApplication, String newState){
        return this.findApplicationById(idApplication)
                .zipWhen(application -> stateUseCase.update(application.getIdState(), newState))
                .flatMap(tuple -> this.notifyUpdate(tuple.getT1().getId(), tuple.getT2().getName(), tuple.getT1().getEmail())
                        .thenReturn(tuple.getT1()));
    }


    public Mono<String> notifyUpdate(UUID idApplication, String newState, String email) {
        return stateNotificationGateway.notifyStateUpdate(idApplication, newState, email);
    }

    public Mono<Application> findApplicationById(UUID idApplication) {
        return applicationRepository.findById(idApplication)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.APPLICATION_LOAN_NOT_FOUND)));

    }

    public Mono<String> calculateDebtCapacity(double interestRate, int termMonths, float loanAmount, UUID idApplication,String email) {
        return Mono.zip(userGateway.getBaseSalary(email), applicationRepository.currentMonthlyDebt(email))
                .flatMap(tuple -> debtCapacityGateway.loanDecision(tuple.getT1(), tuple.getT2(), interestRate, termMonths, loanAmount, idApplication,email));
    }

}
