package br.com.infnet.eventos;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record Evento(UUID eventId, int schemaVersion, String eventType, String aggregateId,
                     long aggregateVersion, Instant occurredAt, JsonNode data) {
    public void validar(String prefixo) {
        if (eventId == null || schemaVersion != 1 || eventType == null || !eventType.startsWith(prefixo)
                || aggregateId == null || aggregateVersion < 0 || occurredAt == null || data == null || !data.isObject()) {
            throw new IllegalArgumentException("Envelope de evento invalido ou versao nao suportada");
        }
    }
}
