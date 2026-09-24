package br.com.infnet.passagens.dtos;

import br.com.infnet.passagens.models.OperacaoHistorico;
import br.com.infnet.passagens.models.PassagemHistorico;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassagemHistoricoResponseDTO {

    private Long id;
    private Long passagemId;
    private OperacaoHistorico operacao;
    private Long passageiroId;
    private String passageiroNome;
    private Integer assento;
    private String origem;
    private String destino;
    private LocalDate data;
    private String status;
    private LocalDateTime registradoEm;

    public static PassagemHistoricoResponseDTO de(PassagemHistorico historico) {
        return new PassagemHistoricoResponseDTO(
                historico.getId(),
                historico.getPassagemId(),
                historico.getOperacao(),
                historico.getPassageiroId(),
                historico.getPassageiroNome(),
                historico.getAssento(),
                historico.getOrigem(),
                historico.getDestino(),
                historico.getData(),
                historico.getStatus(),
                historico.getRegistradoEm()
        );
    }
}
