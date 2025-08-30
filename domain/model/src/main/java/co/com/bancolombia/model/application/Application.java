package co.com.bancolombia.model.application;
import lombok.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Application {
    private UUID id;
    private Integer amount;
    private LocalDate term;
    private String email;
    private String document;
    private UUID idState;
    private UUID idTypeLoan;
}
