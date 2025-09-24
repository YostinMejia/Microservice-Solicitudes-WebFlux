package co.com.bancolombia.r2dbc.application;

import co.com.bancolombia.model.state.States;
import co.com.bancolombia.r2dbc.application.entity.ApplicationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, String>, ReactiveQueryByExampleExecutor<ApplicationEntity> {
    @Query("""
            
                SELECT
                    ROUND(SUM(
                        s.monto * (
                            (tp.tasa_interes / 12) * POWER(1 + (tp.tasa_interes / 12), EXTRACT(MONTH FROM s.plazo))
                        ) / (
                            POWER(1 + (tp.tasa_interes / 12), EXTRACT(MONTH FROM s.plazo)) - 1
                        )
                    ), 2) AS cuota_mensual_total_por_documento,
                    s.email
                FROM
                    solicitud s
                JOIN
                    tipo_prestamo tp ON s.id_tipo_prestamo = tp.id_tipo_prestamo
                JOIN
                	estados e on e.id_estado = s.id_estado
                WHERE e.nombre = 'aprobado' and s.email = :email

                GROUP BY
                    s.email;
                """)

    Mono<Double> currentMonthlyDebt (String email);
}
