package br.com.infnet.passagens;

import br.com.infnet.eventos.*;
import br.com.infnet.passagens.eventos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.infnet.passagens.dtos.PassageiroResponseDTO;
import br.com.infnet.passagens.dtos.PassagemHistoricoResponseDTO;
import br.com.infnet.passagens.dtos.PassagemRequestDTO;
import br.com.infnet.passagens.models.OperacaoHistorico;
import br.com.infnet.passagens.repositories.PassagemRepository;
import br.com.infnet.passagens.services.PassagemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "eureka.client.enabled=false")
@Transactional
class PassagemPersistenciaTests {

    @Autowired
    private PassagemService passagemService;

    @Autowired
    private PassagemRepository passagemRepository;

    @Autowired private PassageiroConsumer passageiroConsumer;
    @Autowired private HistoricoConsumer historicoConsumer;
    @Autowired private OutboxRepository outbox;
    @Autowired private ObjectMapper mapper;

    @BeforeEach
    void sincronizarPassageiros() {
        for (long id = 1; id <= 8; id++) {
            passageiroConsumer.receber(new Evento(java.util.UUID.randomUUID(), 1, "passageiro.criado.v1",
                    Long.toString(id), 0, java.time.Instant.now(), mapper.valueToTree(new PassageiroResponseDTO(
                    id, "Passageiro " + id, "0000000000" + id, "passageiro" + id + "@email.com", "2199999000" + id))));
        }
    }

    private void consumirHistorico() {
        outbox.findAll().forEach(item -> {
            try { historicoConsumer.receber(mapper.readValue(item.getPayload(), Evento.class)); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    void devePersistirPassagemERegistrarHistoricoDeCriacao() {
        var request = new PassagemRequestDTO(
                1L,
                101,
                "Rio de Janeiro",
                "Fortaleza",
                LocalDate.of(2026, 7, 20),
                "Reservada"
        );

        var passagem = passagemService.criar(request);

        assertThat(passagemRepository.findById(passagem.getId())).isPresent();
        assertThat(passagemService.listarHistorico(passagem.getId())).isEmpty();
        assertThat(outbox.countByPublicadoEmIsNull()).isEqualTo(1);

        consumirHistorico();
        var historico = passagemService.listarHistorico(passagem.getId());
        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getOperacao()).isEqualTo(OperacaoHistorico.CRIACAO);
        assertThat(historico.getFirst().getDestino()).isEqualTo("Fortaleza");
        assertThat(historico.getFirst().getPassageiroNome()).isEqualTo("Passageiro 1");
    }

    @Test
    void devePermitirMesmoAssentoEmViagensDiferentes() {
        passagemService.criar(new PassagemRequestDTO(
                2L,
                105,
                "Rio de Janeiro",
                "Fortaleza",
                LocalDate.of(2026, 7, 20),
                "Reservada"
        ));

        var passagem = passagemService.criar(new PassagemRequestDTO(
                3L,
                105,
                "Rio de Janeiro",
                "Fortaleza",
                LocalDate.of(2026, 7, 21),
                "Reservada"
        ));

        assertThat(passagem.getAssento()).isEqualTo(105);
    }

    @Test
    void naoDevePermitirMesmoAssentoNaMesmaViagem() {
        passagemService.criar(new PassagemRequestDTO(
                4L,
                106,
                "Rio de Janeiro",
                "Fortaleza",
                LocalDate.of(2026, 7, 22),
                "Reservada"
        ));

        assertThatThrownBy(() -> passagemService.criar(new PassagemRequestDTO(
                5L,
                106,
                "Rio de Janeiro",
                "Fortaleza",
                LocalDate.of(2026, 7, 22),
                "Reservada"
        ))).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveRegistrarHistoricoDeAtualizacao() {
        var passagem = passagemService.criar(new PassagemRequestDTO(
                6L,
                102,
                "Sao Paulo",
                "Recife",
                LocalDate.of(2026, 8, 5),
                "Reservada"
        ));

        passagemService.atualizar(passagem.getId(), new PassagemRequestDTO(
                7L,
                103,
                "Sao Paulo",
                "Natal",
                LocalDate.of(2026, 8, 6),
                "Confirmada"
        ));

        consumirHistorico();
        var historico = passagemService.listarHistorico(passagem.getId());

        assertThat(historico)
                .extracting(PassagemHistoricoResponseDTO::getOperacao)
                .containsExactly(OperacaoHistorico.CRIACAO, OperacaoHistorico.ATUALIZACAO);
        assertThat(historico.getLast().getDestino()).isEqualTo("Natal");
        assertThat(historico.getLast().getStatus()).isEqualTo("Confirmada");
    }

    @Test
    void deveManterHistoricoAposRemocaoDaPassagem() {
        var passagem = passagemService.criar(new PassagemRequestDTO(
                8L,
                104,
                "Curitiba",
                "Manaus",
                LocalDate.of(2026, 9, 12),
                "Reservada"
        ));

        passagemService.deletar(passagem.getId());

        assertThat(passagemRepository.findById(passagem.getId())).isEmpty();

        consumirHistorico();
        var historico = passagemService.listarHistorico(passagem.getId());
        assertThat(historico)
                .extracting(PassagemHistoricoResponseDTO::getOperacao)
                .containsExactly(OperacaoHistorico.CRIACAO, OperacaoHistorico.REMOCAO);
    }
}
