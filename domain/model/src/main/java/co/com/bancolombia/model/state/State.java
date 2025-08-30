package co.com.bancolombia.model.state;
import lombok.*;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class State {
    private UUID id;
    private String name;
    private String description;
}
