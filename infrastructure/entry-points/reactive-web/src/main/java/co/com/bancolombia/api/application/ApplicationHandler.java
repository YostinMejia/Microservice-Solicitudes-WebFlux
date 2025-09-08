package co.com.bancolombia.api.application;

import co.com.bancolombia.api.application.dto.CreateApplicationDto;
import co.com.bancolombia.api.application.dto.UpdateState;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.api.application.mapper.ApplicationDtoMapper;
import co.com.bancolombia.api.helper.ResponseMapper;
import co.com.bancolombia.model.utils.ResponseCode;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import io.swagger.v3.oas.models.headers.Header;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApplicationHandler {
    private final ApplicationUseCase applicationUseCase;
    private final RequestValidator requestValidator;
    private final ApplicationDtoMapper applicationDtoMapper;

    public Mono<ServerResponse> listenPOSTApplication(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        return serverRequest.bodyToMono(CreateApplicationDto.class)
                .flatMap(requestValidator::validator)
                .flatMap(createApplicationDto ->
                        applicationUseCase.save(
                                        applicationDtoMapper.toApplication(createApplicationDto),
                                        createApplicationDto.typeLoanName(),
                                        createApplicationDto.document(),
                                        createApplicationDto.email(),
                                        authHeader)
                                .map(applicationDtoMapper::toResponseData))
                .map(data -> ResponseMapper.mapBodyResponse(ResponseCode.APPLICATION_CREATED, data))
                .flatMap(responseDto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(responseDto));
    }

    public Mono<ServerResponse> listenUPDATEApplicationState(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        return serverRequest.bodyToMono(UpdateState.class)
                .flatMap(requestValidator::validator)
                .flatMap(updateStateDto -> applicationUseCase.update(updateStateDto.idApplication(), updateStateDto.state(), authHeader))
                .map(data -> ResponseMapper.mapBodyResponse(ResponseCode.APPLICATION_STATE_UPDATED, data))
                .flatMap(responseDto -> ServerResponse.ok().bodyValue(responseDto));

    }
}
