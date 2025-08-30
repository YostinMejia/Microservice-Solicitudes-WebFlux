package co.com.bancolombia.r2dbc.state.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table("estados")
public class StateEntity {
    @Id
    @Column("id_estado")
    private UUID id;
    @Column("nombre")
    private String name;
    @Column("descripcion")
    private String description;
}
