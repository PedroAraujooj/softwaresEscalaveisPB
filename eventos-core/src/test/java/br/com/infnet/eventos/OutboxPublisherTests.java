package br.com.infnet.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class OutboxPublisherTests {
    private final OutboxRepository repository = mock(OutboxRepository.class);
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final OutboxPublisher publisher = new OutboxPublisher(repository, rabbit);

    private Outbox item() {
        var item = new Outbox();
        item.setEventId("evento-123"); item.setRoutingKey("passagem.criada.v1"); item.setPayload("{}");
        when(repository.findTop20ByPublicadoEmIsNullOrderByIdAsc()).thenReturn(List.of(item));
        return item;
    }

    @Test void soMarcaPublicadoAposConfirmacaoPositiva() {
        var item = item();
        doAnswer(call -> {
            CorrelationData data = call.getArgument(3);
            Message message = call.getArgument(2);
            assertThat(message.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
            assertThat(message.getMessageProperties().getMessageId()).isEqualTo(item.getEventId());
            data.getFuture().complete(new CorrelationData.Confirm(true, null)); return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        publisher.publicar();
        assertThat(item.getPublicadoEm()).isNotNull();
    }

    @Test void brokerIndisponivelMantemEventoPendente() {
        var item = item();
        doThrow(new AmqpConnectException(new java.net.ConnectException("offline")))
                .when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        publisher.publicar();
        assertThat(item.getPublicadoEm()).isNull();
        assertThat(item.getTentativas()).isEqualTo(1);
    }

    @Test void mensagemSemRotaMesmoComAckContinuaPendente() {
        var item = item();
        doAnswer(call -> {
            CorrelationData data = call.getArgument(3);
            data.setReturned(new ReturnedMessage(call.getArgument(2), 312, "NO_ROUTE", MessagingConfig.EXCHANGE, item.getRoutingKey()));
            data.getFuture().complete(new CorrelationData.Confirm(true, null)); return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        publisher.publicar();
        assertThat(item.getPublicadoEm()).isNull();
    }

    @Test void nackContinuaPendente() {
        var item = item();
        doAnswer(call -> {
            CorrelationData data = call.getArgument(3);
            data.getFuture().complete(new CorrelationData.Confirm(false, "erro")); return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        publisher.publicar();
        assertThat(item.getPublicadoEm()).isNull();
    }
}
