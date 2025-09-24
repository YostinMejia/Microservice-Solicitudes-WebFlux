package co.com.bancolombia.r2dbc.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationDetails;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.dto.PaginationParams;
import co.com.bancolombia.model.dto.PaginationResponse;
import co.com.bancolombia.model.state.States;
import co.com.bancolombia.model.utils.ResponseCode;
import co.com.bancolombia.r2dbc.application.entity.ApplicationEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Application,
        ApplicationEntity,
        String,
        ApplicationReactiveRepository
        > implements ApplicationRepository {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;


    public ApplicationReactiveRepositoryAdapter(ApplicationReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator, DatabaseClient databaseClient) {
        super(repository, mapper, d -> mapper.map(d, Application.class));
        this.transactionalOperator = transactionalOperator;
        this.databaseClient = databaseClient;

    }


    @Override
    public Mono<Application> save(Application entity) {
        return transactionalOperator.transactional(
                super.save(entity)
        );
    }

    @Override
    public Mono<PaginationResponse<ApplicationDetails>> findByFilter(ApplicationFilter applicationFilter, PaginationParams paginationParams) {
        String baseQuery = """ 
                FROM solicitud s
                JOIN tipo_prestamo t ON s.id_tipo_prestamo = t.id_tipo_prestamo
                JOIN estados e ON e.id_estado = s.id_estado WHERE""" + addDynamicFilter(applicationFilter);

        String countQuery = "SELECT COUNT(*) AS total " + baseQuery;

        String dataQuery = """
                SELECT s.email,
                       s.monto,
                       s.plazo,
                       t.nombre AS tipo_prestamo,
                       t.tasa_interes,
                       e.nombre AS estado_solicitud,
                       (t.tasa_interes / 100) * s.monto AS monto_mensual_solicitud
                """ + baseQuery + " LIMIT :limit OFFSET :offset";

        Mono<Long> totalValues = databaseClient.sql(countQuery)
                .map((row, meta) -> row.get("total", Long.class))
                .one();

        return totalValues.flatMap(total -> {
            int totalPages = (int) Math.ceil((double) total / paginationParams.limit());

            Flux<ApplicationDetails> dataFlux = Flux.range(0, totalPages)
                    .flatMapSequential(page -> {
                        int offset = page * paginationParams.limit();
                        return databaseClient.sql(dataQuery)
                                .bind("limit", paginationParams.limit())
                                .bind("offset", offset)
                                .map((row, meta) -> new ApplicationDetails(
                                        row.get("email", String.class),
                                        row.get("monto", Long.class),
                                        row.get("plazo", LocalDate.class),
                                        row.get("tipo_prestamo", String.class),
                                        row.get("tasa_interes", Integer.class),
                                        row.get("estado_solicitud", String.class),
                                        row.get("monto_mensual_solicitud", Float.class)
                                ))
                                .all();
                    });

            return dataFlux.collectList()
                    .map(allData -> new PaginationResponse<>(
                            ResponseCode.STATES_PAGINATED.getMessage(),
                            ResponseCode.STATES_PAGINATED.getBusinessCode(),
                            paginationParams.limit(),
                            total,
                            allData
                    ));
        });
    }

    @Override
    public Mono<Double> currentMonthlyDebt(String email) {
        return this.repository.currentMonthlyDebt(email);
    }

    private String addDynamicFilter(ApplicationFilter applicationFilter) {
        StringBuilder filter = new StringBuilder();

        String states = applicationFilter.states()
                .filter(list -> !list.isEmpty())
                .orElse(List.of(States.APPROVED.getValue(), States.REJECTED.getValue()))
                .stream()
                .map(state -> String.format("'%s'", state))
                .collect(Collectors.joining(", "));

        filter.append(String.format(" e.nombre IN (%s)", states));

        filter.append(String.format(" OR t.validacion_automatica IS %s", applicationFilter.manualCheck().isPresent() ? "TRUE" : "FALSE"));

        return new String(filter);

    }

    @Override
    public Mono<Application> findById(UUID id) {
        return super.findById(id.toString());
    }


}
