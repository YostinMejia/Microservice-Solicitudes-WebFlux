package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.auth.Role;
import co.com.bancolombia.model.auth.gateway.AuthGateway;

import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
import co.com.bancolombia.model.user.UserGateway;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.model.utils.DefaultProperties;
import co.com.bancolombia.usecase.state.StateUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;


@RequiredArgsConstructor
public class ApplicationUseCase {
    private final ApplicationRepository applicationRepository;
    private final TypeLoanRepository typeLoanRepository;
    private final StateRepository stateRepository;
    private final UserGateway userGateway;
    private final AuthGateway authGateway;
    private final StateUseCase stateUseCase;


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
                        .flatMap(typeLoan -> {
                            Application appWithLoanId = application.toBuilder().idTypeLoan(typeLoan.getId()).build();
                            return stateRepository.save(initialState).map(stateSaved ->
                                    appWithLoanId.toBuilder().idState(stateSaved.getId()).build());

                        })
                        .flatMap(applicationRepository::save);

    }

    public Mono<PaginationResponse<ApplicationDetails>> findByFilter(ApplicationFilter applicationFilter, PaginationParams paginationParams, String authHeader) {
        return authGateway.getRolByAuthHeaderToken(authHeader)
                .filter(email -> !email.isEmpty() || !email.equals(Role.ADVISOR.getValue()))
                .flatMap(x -> applicationRepository.findByFilter(applicationFilter, paginationParams))
                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.UNAUTHORIZED_GET_LOAN_TYPE)));
    }

    public Mono<Application> update(UUID idApplication, String state, String authHeader) {
        return authGateway.getRolByAuthHeaderToken(authHeader)
                .filter(email -> !email.isEmpty() || !email.equals(Role.ADVISOR.getValue()))
                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.UNAUTHORIZED_UPDATE_STATE)))
                .then(Mono.defer(() -> applicationRepository.findById(idApplication)))
                .zipWhen(application -> stateUseCase.update(application.getIdState(), state))
                .map(tuple -> tuple.getT1().toBuilder().idState(tuple.getT2().getId()).build())
                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorCode.APPLICATION_LOAN_NOT_FOUND)));
    }
}
