package br.com.infnet.eventos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.ArrayList;
import java.util.List;

@Configuration @EnableScheduling
public class MessagingConfig {
    public static final String EXCHANGE = "viagens.eventos";
    public static final String DLX = "viagens.dlx";
    public static final String PROJECAO = "passagens.passageiros";
    public static final String HISTORICO = "passagens.historico";
    public static final String NOTIFICACOES = "passagens.notificacoes";

    @Bean
    Jackson2JsonMessageConverter jsonConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    Declarables topologia() {
        var exchange = new TopicExchange(EXCHANGE, true, false);
        var dlx = new DirectExchange(DLX, true, false);
        List<Declarable> declaracoes = new ArrayList<>(List.of(exchange, dlx));
        String[][] rotas = {{PROJECAO, "passageiro.*.v1"}, {HISTORICO, "passagem.*.v1"},
                {NOTIFICACOES, "passagem.criada.v1"}};
        for (var rota : rotas) {
            var fila = QueueBuilder.durable(rota[0]).deadLetterExchange(DLX)
                    .deadLetterRoutingKey(rota[0] + ".dlq").build();
            var dlq = QueueBuilder.durable(rota[0] + ".dlq").build();
            declaracoes.addAll(List.of(fila, dlq, BindingBuilder.bind(fila).to(exchange).with(rota[1]),
                    BindingBuilder.bind(dlq).to(dlx).with(rota[0] + ".dlq")));
        }
        return new Declarables(declaracoes);
    }
}
