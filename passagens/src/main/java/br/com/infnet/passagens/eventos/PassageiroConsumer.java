package br.com.infnet.passagens.eventos;

import br.com.infnet.eventos.*;
import br.com.infnet.passagens.dtos.PassageiroResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service @RequiredArgsConstructor
public class PassageiroConsumer {
    private final PassageiroProjecaoRepository repository;
    private final ObjectMapper mapper;

    @RabbitListener(queues = MessagingConfig.PROJECAO)
    @Transactional
    public void receber(Evento evento) {
        evento.validar("passageiro.");
        if (!Set.of("passageiro.criado.v1", "passageiro.atualizado.v1", "passageiro.removido.v1").contains(evento.eventType())) {
            throw new IllegalArgumentException("Tipo de evento desconhecido");
        }
        var dados = mapper.convertValue(evento.data(), PassageiroResponseDTO.class);
        if (dados.getId() == null || !dados.getId().toString().equals(evento.aggregateId()) || dados.getNome() == null) {
            throw new IllegalArgumentException("Snapshot de passageiro invalido");
        }
        var projecao = repository.findById(dados.getId()).orElseGet(PassageiroProjecao::new);
        // Ignora duplicatas e eventos antigos, inclusive apos a remocao (tombstone).
        if (evento.aggregateVersion() <= projecao.getEventVersion()) return;
        projecao.setId(dados.getId());
        projecao.setEventVersion(evento.aggregateVersion());
        projecao.setNome(dados.getNome());
        projecao.setCpf(dados.getCpf());
        projecao.setEmail(dados.getEmail());
        projecao.setTelefone(dados.getTelefone());
        projecao.setRemovido(evento.eventType().equals("passageiro.removido.v1"));
        repository.saveAndFlush(projecao);
    }
}
