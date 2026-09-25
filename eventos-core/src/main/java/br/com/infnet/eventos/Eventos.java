package br.com.infnet.eventos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.Instant;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class Eventos {
    private final OutboxRepository repository;
    private final ObjectMapper mapper;

    // Impede publicar fora da transacao que alterou o agregado.
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String tipo, Long agregadoId, long versao, Object dados) {
        var evento = new Evento(UUID.randomUUID(), 1, tipo, agregadoId.toString(), versao,
                Instant.now(), mapper.valueToTree(dados));
        var outbox = new Outbox();
        outbox.setEventId(evento.eventId().toString());
        outbox.setRoutingKey(tipo);
        try {
            outbox.setPayload(mapper.writeValueAsString(evento));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Falha ao serializar evento", e);
        }
        repository.save(outbox);
    }
}
