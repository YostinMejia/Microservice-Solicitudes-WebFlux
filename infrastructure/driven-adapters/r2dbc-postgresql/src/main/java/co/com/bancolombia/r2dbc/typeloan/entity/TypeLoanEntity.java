package co.com.bancolombia.r2dbc.typeloan.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tipo_prestamo")
public class TypeLoanEntity {
    @Id
    @Column("id_tipo_prestamo")
    private UUID id;
    @Column("nombre")
    private String name;
    @Column("monto_minimo")
    private Integer minimumAmount;
    @Column("monto_maximo")
    private Integer maximumAmount;
    @Column("tasa_interes")
    private Double interestRate;
    @Column("validacion_automatica")
    private Boolean automaticValidation;
}
