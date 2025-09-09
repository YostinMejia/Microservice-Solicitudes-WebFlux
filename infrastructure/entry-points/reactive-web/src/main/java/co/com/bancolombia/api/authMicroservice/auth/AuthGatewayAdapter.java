package co.com.bancolombia.api.authMicroservice.auth;

import co.com.bancolombia.api.authMicroservice.auth.config.AuthPath;
import co.com.bancolombia.api.authMicroservice.auth.dto.SameEmailAsTokenDto;
import co.com.bancolombia.model.auth.gateway.AuthGateway;
import co.com.bancolombia.model.dto.Response;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.utils.BusinessErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthGatewayAdapter implements AuthGateway {

    private final AuthPath authPath;
    private final WebClient webClient;

    @Override
    public Mono<Boolean> isSameEmailAsToken(String email, String authHeader) {

        return webClient.post()
                .uri(authPath.getIsSameEmailAsToken())
                .header("Authorization", authHeader)
                .bodyValue(new SameEmailAsTokenDto(email))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    String message = (String) body.get("message");
                                    String code = (String) body.get("code");
                                    return Mono.error(new BusinessException(message, code));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Response<Boolean>>() {
                })
                .flatMap(response -> Mono.just(response.data()));
    }

    @Override
    public Mono<String> getRolByAuthHeaderToken(String authHeader) {
        return webClient.get()
                .uri(authPath.getGetRoleByAuthHeaderToken())
                .header("Authorization", authHeader)
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    String message = (String) body.get("message");
                                    String code = (String) body.get("code");
                                    return Mono.error(new BusinessException(message, code));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Response<String>>() {
                })
                .flatMap(response -> Mono.just(response.data()));

    }
}
