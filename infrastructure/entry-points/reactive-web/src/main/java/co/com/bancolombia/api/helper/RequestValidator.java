package co.com.bancolombia.api.helper;

import co.com.bancolombia.api.dto.CreateApplicationDto;
import co.com.bancolombia.model.exceptions.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public Mono<CreateApplicationDto> validator(CreateApplicationDto createApplicationDto) {

        return Mono.defer(() -> {
            final Set<ConstraintViolation<CreateApplicationDto>> errors = validator.validate(createApplicationDto);
            if (errors.isEmpty()) {
                return Mono.just(createApplicationDto);
            }

            final List<String> listErrors = errors.stream().map(er -> String.format("%s: %s", er.getPropertyPath(), er.getMessage())).toList();
            return Mono.error(new BusinessException(listErrors, "Create application validation failed", "B400-00"));

        });

    }

}
