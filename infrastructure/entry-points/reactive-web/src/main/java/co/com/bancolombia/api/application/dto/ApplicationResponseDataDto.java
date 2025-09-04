package co.com.bancolombia.api.application.dto;

import java.util.UUID;

public record ApplicationResponseDataDto(
        Integer amount,
        String term,
        String document,
        String email,
        UUID idState,
        UUID idTypeLoan

) {
}
