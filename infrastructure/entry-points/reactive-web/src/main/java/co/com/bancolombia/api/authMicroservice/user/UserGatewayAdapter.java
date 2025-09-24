package co.com.bancolombia.api.authMicroservice.user;

import co.com.bancolombia.api.authMicroservice.user.config.UserPath;
import co.com.bancolombia.api.authMicroservice.user.dto.ExistsByDocumentAndEmailDto;
import co.com.bancolombia.model.dto.Response;
import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.user.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserGatewayAdapter implements UserGateway {

    private final UserPath userPath;
    private final WebClient webClient;

    @Override
    public Mono<Boolean> existByDocumentAndEmail(String document, String email , String authHeader) {
        return webClient.post()
                .uri(userPath.getExistsByDocumentAndEmail())
                .header("Authorization", authHeader)
                .bodyValue(new ExistsByDocumentAndEmailDto(document, email))
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
    public Mono<Long> getBaseSalary(String email) {
        return webClient.get()
                .uri(userPath.getBaseSalary(),email)
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
                .bodyToMono(new ParameterizedTypeReference<Response<Long>>() {
                })
                .map(Response::data);
    }

}
