package co.com.bancolombia.api.helper;

import co.com.bancolombia.api.config.UserPath;
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
    public Mono<Boolean> existByDocument(String document) {
        return webClient.get()
                .uri(userPath.getExistsByDocument(), document)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDto<Boolean>>() {
                })
                .flatMap(response -> Mono.just(response.data()));
    }
}
