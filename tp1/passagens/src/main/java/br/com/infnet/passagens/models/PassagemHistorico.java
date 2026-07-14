package br.com.infnet.passagens.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "passagens_historico",
        indexes = {
                @Index(name = "idx_passagem_historico_passagem_id", columnList = "passagem_id"),
                @Index(name = "idx_passagem_historico_operacao", columnList = "operacao")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassagemHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passagem_id", nullable = false)
    private Long passagemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperacaoHistorico operacao;

    @Column(nullable = false, length = 120)
    private String passageiro;

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

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    public static PassagemHistorico registrar(Passagem passagem, OperacaoHistorico operacao) {
        PassagemHistorico historico = new PassagemHistorico();
        historico.setPassagemId(passagem.getId());
        historico.setOperacao(operacao);
        historico.setPassageiro(passagem.getPassageiro());
        historico.setAssento(passagem.getAssento());
        historico.setOrigem(passagem.getOrigem());
        historico.setDestino(passagem.getDestino());
        historico.setData(passagem.getData());
        historico.setStatus(passagem.getStatus());
        historico.setRegistradoEm(LocalDateTime.now());
        return historico;
    }
}
