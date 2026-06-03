package br.com.infnet.passagens.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "passagens")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Passagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String passageiro;
    private Integer assento;
    private String origem;
    private String destino;
    @Column(name = "data_viagem")
    private LocalDate data;
    private String status;
}