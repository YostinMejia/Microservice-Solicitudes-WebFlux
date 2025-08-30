package co.com.bancolombia.api;

import co.com.bancolombia.model.dto.MultipleErrorsResponseDto;
import co.com.bancolombia.model.dto.SingleErrorResponseDto;
import co.com.bancolombia.model.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(serverCodecConfigurer.getWriters());
        this.setMessageReaders(serverCodecConfigurer.getReaders());

    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions
                .route(RequestPredicates.all(), this::handleErrors);

    }

    private Mono<ServerResponse> handleErrors(ServerRequest request) {
        return Mono.just(getError(request))
                .doOnNext(er -> log.error(er.getMessage()))
                .flatMap(error -> {
                    if (error instanceof BusinessException bex) {
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(
                                        bex.getErrors() != null ?
                                                new MultipleErrorsResponseDto(bex.getErrors(), bex.getMessage(), bex.getCode())
                                                : new SingleErrorResponseDto(bex.getMessage(), bex.getCode()));
                    }
                    return Mono.error(error);
                }).onErrorResume(throwable -> {
                    Map<String, Object> errorAttributes = getErrorAttributes(
                            request,
                            ErrorAttributeOptions.of(
                                    ErrorAttributeOptions.Include.STATUS,
                                    ErrorAttributeOptions.Include.ERROR
                            )
                    );

                    return ServerResponse
                            .status((int) errorAttributes.getOrDefault("status", 500))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new SingleErrorResponseDto(
                                    (String) errorAttributes.getOrDefault("error", "Internal Server Error"),
                                    "I500-00"
                            ));
                });

    }

}
