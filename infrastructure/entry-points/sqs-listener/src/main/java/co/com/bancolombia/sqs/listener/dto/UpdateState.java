package co.com.bancolombia.sqs.listener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateState(
        @NotNull
        @Pattern(regexp = "aprobada|rechazada", message = "state: The new state should be either aprobada or rechazada")
        String state,
        @NotNull(message = "idApplication: should not be empty and UUID format")
        UUID idApplication,
        @NotNull
        @Email
        String email
) {
}
