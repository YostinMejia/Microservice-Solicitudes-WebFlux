package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.TransactionalOperatorGateway;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.state.State;
import co.com.bancolombia.model.state.gateways.StateRepository;
import co.com.bancolombia.model.typeloan.gateways.TypeLoanRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
public class ApplicationUseCase {
    private final ApplicationRepository applicationRepository;
    private final TypeLoanRepository typeLoanRepository;
    private final StateRepository stateRepository;
    private final TransactionalOperatorGateway transactionalOperatorGateway;

    public Mono<Application> save(Application application, String typeLoanName) {
        final State initialState = new State().toBuilder().name("Pendiente de revisión").build();

        return transactionalOperatorGateway.execute(

                typeLoanRepository.findByName(typeLoanName)
                .switchIfEmpty(Mono.error(new BusinessException(null, "type of loan does not exist", "B400-00")))
                .flatMap(typeLoan -> {
                    Application application1 = application.toBuilder().idTypeLoan(typeLoan.getId()).build();
                    return stateRepository.save(initialState).map(stateSaved -> {
                        return application1.toBuilder().idState(stateSaved.getId()).build();
                    });
                })
                .flatMap(applicationRepository::save)
        );
    }
}
