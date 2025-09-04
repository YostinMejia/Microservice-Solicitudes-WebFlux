package co.com.bancolombia.api.authMicroservice.user;

import co.com.bancolombia.api.authMicroservice.user.config.UserPath;
import co.com.bancolombia.api.authMicroservice.user.dto.ExistsByDocumentAndEmailDto;
import co.com.bancolombia.model.dto.ResponseDto;
import co.com.bancolombia.model.user.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<Boolean>>() {
                })
                .flatMap(response -> Mono.just(response.data()));
    }

}
