package br.com.infnet.passagens.eventos;

import br.com.infnet.eventos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service @RequiredArgsConstructor
public class NotificacaoConsumer {
    private final NotificacaoRepository repository;

    @RabbitListener(queues = MessagingConfig.NOTIFICACOES)
    @Transactional
    public void receber(Evento evento) {
        evento.validar("passagem.");
        if (!"passagem.criada.v1".equals(evento.eventType()) || !evento.data().hasNonNull("passagemId")
                || !evento.aggregateId().equals(evento.data().get("passagemId").asText())) {
            throw new IllegalArgumentException("Evento de notificacao invalido");
        }
        if (repository.existsById(evento.eventId().toString())) return;
        var notificacao = new Notificacao();
        notificacao.setEventId(evento.eventId().toString());
        notificacao.setPassagemId(Long.valueOf(evento.aggregateId()));
        notificacao.setMensagem("SIMULACAO: passagem " + evento.aggregateId() + " criada.");
        notificacao.setRegistradaEm(Instant.now());
        repository.saveAndFlush(notificacao);
    }
}
