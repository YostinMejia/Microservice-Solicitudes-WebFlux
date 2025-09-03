package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CreateApplicationDto;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.api.mapper.ApplicationDtoMapper;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.dto.ResponseDto;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import co.com.bancolombia.model.utils.ResponseCode;
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

    public Mono<ServerResponse> listenPOSTApplication(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateApplicationDto.class)
                .flatMap(requestValidator::validator)
                .flatMap(createApplicationDto -> applicationUseCase.save(applicationDtoMapper.toApplication(createApplicationDto), createApplicationDto.typeLoanName(), createApplicationDto.document()))
                .flatMap(application -> ServerResponse.status(201).bodyValue(new ResponseDto<Application>(ResponseCode.APPLICATION_CREATED.getMessage(), ResponseCode.APPLICATION_CREATED.getCode(), application)));
    }
}
