package co.com.bancolombia.model.typeloan;
import lombok.*;

import java.util.UUID;
import lombok.NoArgsConstructor;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TypeLoan {
    private UUID id;
    private String name;
    private Integer minimumAmount;
    private Integer maximumAmount;
    private Double interestRate;
    private Boolean automaticValidation;
}
