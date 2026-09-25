package br.com.infnet.passagens.eventos;

import br.com.infnet.eventos.*;
import br.com.infnet.passagens.models.*;
import br.com.infnet.passagens.repositories.PassagemHistoricoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service @RequiredArgsConstructor
public class HistoricoConsumer {
    private final PassagemHistoricoRepository repository;
    private final ObjectMapper mapper;

    @RabbitListener(queues = MessagingConfig.HISTORICO)
    @Transactional
    public void receber(Evento evento) {
        evento.validar("passagem.");
        OperacaoHistorico operacao = switch (evento.eventType()) {
            case "passagem.criada.v1" -> OperacaoHistorico.CRIACAO;
            case "passagem.atualizada.v1" -> OperacaoHistorico.ATUALIZACAO;
            case "passagem.removida.v1" -> OperacaoHistorico.REMOCAO;
            default -> throw new IllegalArgumentException("Tipo de evento desconhecido");
        };
        if (repository.existsByEventId(evento.eventId().toString())) return;
        var historico = mapper.convertValue(evento.data(), PassagemHistorico.class);
        if (historico.getPassagemId() == null || !historico.getPassagemId().toString().equals(evento.aggregateId())) {
            throw new IllegalArgumentException("Snapshot de passagem invalido");
        }
        historico.setId(null);
        historico.setEventId(evento.eventId().toString());
        historico.setOperacao(operacao);
        historico.setRegistradoEm(LocalDateTime.ofInstant(evento.occurredAt(), ZoneOffset.UTC));
        // A restricao unique cobre entregas concorrentes; a repeticao apos rollback encontra o registro.
        repository.saveAndFlush(historico);
    }
}
