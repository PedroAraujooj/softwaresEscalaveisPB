package br.com.infnet.passagens;

import br.com.infnet.passagens.dtos.PassagemHistoricoResponseDTO;
import br.com.infnet.passagens.dtos.PassagemRequestDTO;
import br.com.infnet.passagens.models.OperacaoHistorico;
import br.com.infnet.passagens.repositories.PassagemRepository;
import br.com.infnet.passagens.services.PassagemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PassagemPersistenciaTests {

    @Autowired
    private PassagemService passagemService;

    @Autowired
    private PassagemRepository passagemRepository;

    @Test
    void devePersistirPassagemERegistrarHistoricoDeCriacao() {
        var request = new PassagemRequestDTO(
                "Ana Costa",
                101,
                "Rio de Janeiro",
                "Fortaleza",
                LocalDate.of(2026, 7, 20),
                "Reservada"
        );

        var passagem = passagemService.criar(request);

        assertThat(passagemRepository.findById(passagem.getId())).isPresent();

        var historico = passagemService.listarHistorico(passagem.getId());
        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getOperacao()).isEqualTo(OperacaoHistorico.CRIACAO);
        assertThat(historico.getFirst().getDestino()).isEqualTo("Fortaleza");
    }

    @Test
    void devePermitirMesmoAssentoEmViagensDiferentes() {
        passagemService.criar(new PassagemRequestDTO(
                "Daniel Souza",
                105,
                "Rio de Janeiro",
                "Fortaleza",
                LocalDate.of(2026, 7, 20),
                "Reservada"
        ));

        var passagem = passagemService.criar(new PassagemRequestDTO(
                "Eduarda Nunes",
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
                "Felipe Rocha",
                106,
                "Rio de Janeiro",
                "Fortaleza",
                LocalDate.of(2026, 7, 22),
                "Reservada"
        ));

        assertThatThrownBy(() -> passagemService.criar(new PassagemRequestDTO(
                "Giovana Alves",
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
                "Bruno Lima",
                102,
                "Sao Paulo",
                "Recife",
                LocalDate.of(2026, 8, 5),
                "Reservada"
        ));

        passagemService.atualizar(passagem.getId(), new PassagemRequestDTO(
                "Bruno Lima",
                103,
                "Sao Paulo",
                "Natal",
                LocalDate.of(2026, 8, 6),
                "Confirmada"
        ));

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
                "Carla Mendes",
                104,
                "Curitiba",
                "Manaus",
                LocalDate.of(2026, 9, 12),
                "Reservada"
        ));

        passagemService.deletar(passagem.getId());

        assertThat(passagemRepository.findById(passagem.getId())).isEmpty();

        var historico = passagemService.listarHistorico(passagem.getId());
        assertThat(historico)
                .extracting(PassagemHistoricoResponseDTO::getOperacao)
                .containsExactly(OperacaoHistorico.CRIACAO, OperacaoHistorico.REMOCAO);
    }
}
