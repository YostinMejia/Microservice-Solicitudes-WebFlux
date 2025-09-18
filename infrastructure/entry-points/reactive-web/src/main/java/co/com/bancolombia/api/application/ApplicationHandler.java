package co.com.bancolombia.api.application;

import co.com.bancolombia.api.application.dto.CreateApplicationDto;
import co.com.bancolombia.api.application.dto.DebtCapacityDto;
import co.com.bancolombia.api.application.dto.UpdateState;
import co.com.bancolombia.api.helper.RequestValidator;
import co.com.bancolombia.api.application.mapper.ApplicationDtoMapper;
import co.com.bancolombia.api.helper.ResponseMapper;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.dto.PaginationParams;
import co.com.bancolombia.model.dto.PaginationResponse;
import co.com.bancolombia.model.utils.ResponseCode;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    public Mono<ServerResponse> listenGETFindByFilter(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        int limit = serverRequest.queryParam("limit").isPresent() ? Integer.parseInt(serverRequest.queryParam("limit").get()) : 10;
        int page = serverRequest.queryParam("page").isPresent()? Integer.parseInt(serverRequest.queryParam("page").get()) : 1;
        Optional<List<String>> states = serverRequest.queryParam("states").map(s -> Arrays.asList(s.split(",")));
        Optional<Boolean> manualCheck = serverRequest.queryParam("manualCheck").map(Boolean::parseBoolean);
        return ServerResponse.ok().body(applicationUseCase.findByFilter(new ApplicationFilter(states,manualCheck), new PaginationParams(limit, page), authHeader), PaginationResponse.class);

    }

    public Mono<ServerResponse> listenUPDATEApplicationState(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        return serverRequest.bodyToMono(UpdateState.class)
                .flatMap(requestValidator::validator)
                .flatMap(updateStateDto -> applicationUseCase.update(updateStateDto.idApplication(), updateStateDto.state(), authHeader))
                .map(data -> ResponseMapper.mapBodyResponse(ResponseCode.APPLICATION_STATE_UPDATED, data))
                .flatMap(responseDto -> ServerResponse.ok().bodyValue(responseDto));

    }

    public Mono<ServerResponse> listenCalculateDebtCapacity(ServerRequest serverRequest){
        return serverRequest.bodyToMono(DebtCapacityDto.class)
                .flatMap(requestValidator::validator)
                .flatMap(debtCapacityDto -> applicationUseCase.debtCapacity(
                        debtCapacityDto.totalIncome(),
                        debtCapacityDto.currentMonthlyDebt(),
                        debtCapacityDto.interestRate(),
                        debtCapacityDto.termMonths(),
                        debtCapacityDto.loanAmount()
                ))
                .map(messageId->ResponseMapper.mapBodyResponse(ResponseCode.CALCULATE_DEBT_CAPACITY_CREATED,messageId))
                .flatMap(responseDto->ServerResponse.ok().bodyValue(responseDto));
    }
}
