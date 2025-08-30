package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CreateApplicationDto;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.api.mapper.ApplicationDtoMapper;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.dto.ResponseDto;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ApplicationHandler {
    private final ApplicationUseCase applicationUseCase;
    private final RequestValidator requestValidator;
    private final ApplicationDtoMapper applicationDtoMapper;

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateApplicationDto.class)
                .flatMap(requestValidator::validator)
                .doOnNext(System.out::println)
                .flatMap(createApplicationDto -> applicationUseCase.save(applicationDtoMapper.toApplication(createApplicationDto), createApplicationDto.typeLoanName()))
                .doOnNext(System.out::println)
                .flatMap(application -> ServerResponse.status(201).bodyValue(new ResponseDto<Application>("Application created successfully", "201-00", application)));
    }
}
