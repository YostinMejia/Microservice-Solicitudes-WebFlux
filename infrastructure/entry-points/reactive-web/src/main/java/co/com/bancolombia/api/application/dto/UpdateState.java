package co.com.bancolombia.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record UpdateState(
        @NotNull
        @Pattern(regexp = "aprobada|rechazada", message = "state: The new state should be either aprobada or rechazada")
        String state,
        @NotNull(message = "idApplication: should not be empty and UUID format")
        UUID idApplication
) {
}
