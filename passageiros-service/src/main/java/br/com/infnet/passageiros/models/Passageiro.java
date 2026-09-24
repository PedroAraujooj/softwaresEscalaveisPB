package br.com.infnet.passageiros.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "passageiros",
        indexes = {
                @Index(name = "idx_passageiro_nome", columnList = "nome"),
                @Index(name = "idx_passageiro_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_passageiro_cpf", columnNames = "cpf"),
                @UniqueConstraint(name = "uk_passageiro_email", columnNames = "email")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Passageiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;
}
