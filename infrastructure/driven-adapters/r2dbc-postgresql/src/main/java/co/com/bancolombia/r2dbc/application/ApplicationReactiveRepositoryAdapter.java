package co.com.bancolombia.r2dbc.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.ApplicationDetailsDto;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.dto.PaginationParams;
import co.com.bancolombia.model.dto.PaginationResponseDto;
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
import java.util.Optional;
import java.util.UUID;

@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Application,
        ApplicationEntity,
        String,
        ApplicationReactiveRepository
        > implements ApplicationRepository {
    public ApplicationReactiveRepositoryAdapter(ApplicationReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator, DatabaseClient databaseClient) {
        super(repository, mapper, d -> mapper.map(d, Application.class));
        this.transactionalOperator = transactionalOperator;
        this.databaseClient = databaseClient;

    }
    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Application> save(Application entity) {
        return transactionalOperator.transactional(
                super.save(entity)
        );
    }

    @Override
    public Mono<Application> findById(UUID id) {
        return super.findById(id.toString());
    }
    @Override
    public Mono<PaginationResponseDto<ApplicationDetailsDto>> findByLoanType(int limit) {
        String baseQuery = """ 
        FROM solicitud s
        JOIN tipo_prestamo t ON s.id_tipo_prestamo = t.id_tipo_prestamo
        JOIN estados e ON e.id_estado = s.id_estado
        WHERE e.nombre IN ('pendiente', 'rechazada')
        OR t.validacion_automatica IS TRUE
    """;

        String countQuery = "SELECT COUNT(*) AS total " + baseQuery;

        String dataQuery = """
        SELECT s.email,
               s.monto,
               s.plazo,
               t.nombre AS tipo_prestamo,
               t.tasa_interes,
               e.nombre AS estado_solicitud,
               (t.tasa_interes::numeric / 100) * s.monto AS monto_mensual_solicitud
        """ + baseQuery + " LIMIT :limit OFFSET :offset";

        Mono<Long> totalValues = databaseClient.sql(countQuery)
                .map((row, meta) -> row.get("total", Long.class))
                .one();

        return totalValues.flatMap(total -> {
            int totalPages = (int) Math.ceil((double) total / limit);

            Flux<ApplicationDetailsDto> dataFlux = Flux.range(0, totalPages)
                    .flatMapSequential(page -> {
                        int offset = page * limit;
                        return databaseClient.sql(dataQuery)
                                .bind("limit", limit)
                                .bind("offset", offset)
                                .map((row, meta) -> new ApplicationDetailsDto(
                                        row.get("email", String.class),
                                        row.get("monto", Long.class),
                                        row.get("plazo", LocalDate.class),
                                        row.get("tipo_prestamo", String.class),
                                        row.get("tasa_interes", Integer.class),
                                        row.get("estado_solicitud", String.class),
                                        row.get("monto_mensual_solicitud", Long.class)
                                ))
                                .all();
                    });

            return dataFlux.collectList()
                    .map(allData -> new PaginationResponseDto<>(
                            ResponseCode.STATES_PAGINATED.getMessage(),
                            ResponseCode.STATES_PAGINATED.getBusinessCode(),
                            limit,
                            total,
                            allData
                    ));
        });
    }



}
