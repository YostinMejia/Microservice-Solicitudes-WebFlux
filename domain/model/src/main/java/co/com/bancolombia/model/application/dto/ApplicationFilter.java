package co.com.bancolombia.model.application.dto;

import java.util.List;
import java.util.Optional;

public record ApplicationFilter(
        Optional<List<String>> states,
        Optional<Boolean> manualCheck
) {
}
