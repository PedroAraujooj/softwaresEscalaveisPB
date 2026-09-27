package br.com.infnet.eventos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component @RequiredArgsConstructor
public class RastreamentoEventos {
    private final Tracer tracer;
    private final Propagator propagator;
    private final ObjectMapper mapper;

    public String capturar() {
        var atual = tracer.currentSpan();
        if (atual == null) return null;
        Map<String, String> headers = new HashMap<>();
        propagator.inject(atual.context(), headers, Map::put);
        try {
            return mapper.writeValueAsString(headers);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao salvar contexto do trace", e);
        }
    }

    public Span iniciar(Outbox item) {
        Map<String, String> headers = new HashMap<>();
        if (item.getTraceContext() != null) {
            try {
                headers = mapper.readValue(item.getTraceContext(), new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                throw new IllegalStateException("Contexto do trace invalido", e);
            }
        }
        return propagator.extract(headers, Map::get).name("outbox.publish")
                .tag("event.id", item.getEventId()).tag("event.type", item.getRoutingKey()).start();
    }

    public Tracer.SpanInScope ativar(Span span) {
        return tracer.withSpan(span);
    }
}
