package co.com.bancolombia.api.authMicroservice.auth;

import co.com.bancolombia.api.authMicroservice.auth.config.AuthPath;
import co.com.bancolombia.api.authMicroservice.auth.dto.SameEmailAsTokenDto;
import co.com.bancolombia.model.auth.AuthGateway;
import co.com.bancolombia.model.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<Boolean>>() {
                })
                .flatMap(response -> Mono.just(response.data()));
    }
}
