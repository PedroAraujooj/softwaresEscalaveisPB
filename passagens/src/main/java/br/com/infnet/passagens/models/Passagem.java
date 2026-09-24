package br.com.infnet.passagens.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "passagens",
        indexes = {
                @Index(name = "idx_passagem_destino", columnList = "destino"),
                @Index(name = "idx_passagem_data_viagem", columnList = "data_viagem"),
                @Index(name = "idx_passagem_status", columnList = "status")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_passagem_assento_viagem",
                columnNames = {"assento", "origem", "destino", "data_viagem"}
        )
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Passagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long passageiroId;

    @Column(nullable = false)
    private Integer assento;

    @Column(nullable = false, length = 80)
    private String origem;

    @Column(nullable = false, length = 80)
    private String destino;

    @Column(name = "data_viagem", nullable = false)
    private LocalDate data;

    @Column(nullable = false, length = 30)
    private String status;
}
