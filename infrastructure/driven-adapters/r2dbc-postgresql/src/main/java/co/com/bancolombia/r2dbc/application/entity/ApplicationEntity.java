package co.com.bancolombia.r2dbc.application.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table("solicitud")
public class ApplicationEntity {
    @Id
    @Column("id_solicitud")
    private UUID id;
    @Column("monto")
    private Integer amount;
    @Column("plazo")
    private LocalDate term;
    @Column("email")
    private String email;
    @Column("documento")
    private String document;
    @Column("id_estado")
    private UUID idState;
    @Column("id_tipo_prestamo")
    private UUID idTypeLoan;
}
