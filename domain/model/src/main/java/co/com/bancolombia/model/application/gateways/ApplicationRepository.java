package co.com.bancolombia.model.application.gateways;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationDetails;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.dto.PaginationParams;
import co.com.bancolombia.model.dto.PaginationResponse;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {
    Mono<Application> save(Application application);

    Mono<PaginationResponse<ApplicationDetails>> findByFilter(ApplicationFilter applicationFilter, PaginationParams paginationParams);

}
