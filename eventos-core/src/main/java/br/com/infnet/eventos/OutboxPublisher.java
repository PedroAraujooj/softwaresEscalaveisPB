package br.com.infnet.eventos;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service @RequiredArgsConstructor
@ConditionalOnProperty(name = "eventos.publisher.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxRepository repository;
    private final RabbitTemplate rabbit;

    @Scheduled(fixedDelayString = "${eventos.publisher.delay-ms:1000}")
    @Transactional
    public void publicar() {
        for (var item : repository.findTop20ByPublicadoEmIsNullOrderByIdAsc()) {
            item.setTentativas(item.getTentativas() + 1);
            try {
                var message = MessageBuilder.withBody(item.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .setMessageId(item.getEventId()).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
                var correlation = new CorrelationData(item.getEventId());
                rabbit.send(MessagingConfig.EXCHANGE, item.getRoutingKey(), message, correlation);
                var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
                if (!confirm.isAck() || correlation.getReturned() != null) {
                    throw new IllegalStateException("Evento rejeitado ou sem fila de destino");
                }
                item.setPublicadoEm(Instant.now());
                log.info("Evento publicado eventId={} tipo={}", item.getEventId(), item.getRoutingKey());
            } catch (Exception e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                log.warn("Outbox pendente eventId={} tentativa={} motivo={}", item.getEventId(), item.getTentativas(), e.toString());
                // Preserva a ordem; o proximo ciclo tentara novamente com o mesmo eventId.
                break;
            }
        }
    }
}
