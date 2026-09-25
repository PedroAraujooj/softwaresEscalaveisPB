package br.com.infnet.passagens;

import br.com.infnet.eventos.*;
import br.com.infnet.passagens.dtos.*;
import br.com.infnet.passagens.eventos.*;
import br.com.infnet.passagens.models.*;
import br.com.infnet.passagens.repositories.*;
import br.com.infnet.passagens.services.PassagemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "eureka.client.enabled=false")
class EventosTests {
    @Autowired PassageiroConsumer passageiros;
    @Autowired PassageiroProjecaoRepository projecoes;
    @Autowired HistoricoConsumer historicos;
    @Autowired PassagemHistoricoRepository historicoRepository;
    @Autowired NotificacaoConsumer notificacoes;
    @Autowired NotificacaoRepository notificacaoRepository;
    @Autowired ObjectMapper mapper;
    @Autowired OutboxRepository outbox;
    @Autowired PassagemRepository passagens;
    @Autowired PassagemService service;
    @Autowired PlatformTransactionManager transactionManager;

    private Evento passageiro(long id, long versao, String tipo, String nome) {
        return new Evento(UUID.randomUUID(), 1, tipo, Long.toString(id), versao, Instant.now(),
                mapper.valueToTree(new PassageiroResponseDTO(id, nome, "12345678901", "a@a.com", "21999999999")));
    }

    @Test void ignoraDuplicatasEventosAntigosENaoRessuscitaRemovido() {
        var novo = passageiro(801, 2, "passageiro.atualizado.v1", "Nome novo");
        passageiros.receber(novo);
        passageiros.receber(novo);
        passageiros.receber(passageiro(801, 0, "passageiro.criado.v1", "Nome antigo"));
        assertThat(projecoes.findById(801L).orElseThrow().getNome()).isEqualTo("Nome novo");
        passageiros.receber(passageiro(801, 3, "passageiro.removido.v1", "Nome novo"));
        passageiros.receber(novo);
        assertThat(projecoes.findById(801L).orElseThrow().isRemovido()).isTrue();
        assertThatThrownBy(() -> service.criar(new PassagemRequestDTO(801L, 999, "A", "B", LocalDate.now(), "Reservada")))
                .hasMessageContaining("Passageiro removido");
    }

    @Test void gravaHistoricoENotificacaoUmaVezMesmoRecebendoDuasVezes() {
        var passagem = new Passagem(900L, 801L, 20, "A", "B", LocalDate.now(), "Reservada");
        var snapshot = PassagemHistorico.registrar(passagem, OperacaoHistorico.CRIACAO, "Teste");
        var evento = new Evento(UUID.randomUUID(), 1, "passagem.criada.v1", "900", 0, Instant.now(), mapper.valueToTree(snapshot));
        historicos.receber(evento);
        historicos.receber(evento);
        notificacoes.receber(evento);
        notificacoes.receber(evento);
        assertThat(historicoRepository.findByPassagemIdOrderByRegistradoEmAscIdAsc(900L)).hasSize(1);
        assertThat(notificacaoRepository.findById(evento.eventId().toString())).isPresent();
    }

    @Test void rollbackDesfazPassagemEOutboxJuntos() {
        passageiros.receber(passageiro(802, 0, "passageiro.criado.v1", "Teste"));
        long antes = outbox.count();
        long passagensAntes = passagens.count();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            service.criar(new PassagemRequestDTO(802L, 802, "Teste rollback", "B", LocalDate.now(), "Reservada"));
            assertThat(outbox.count()).isEqualTo(antes + 1);
            status.setRollbackOnly();
        });
        assertThat(outbox.count()).isEqualTo(antes);
        assertThat(passagens.count()).isEqualTo(passagensAntes);
    }

    @Test void passageiroAindaNaoSincronizadoNaoCriaPassagemNemEvento() {
        long antes = outbox.count();
        assertThatThrownBy(() -> service.criar(new PassagemRequestDTO(99999L, 1, "A", "B", LocalDate.now(), "Reservada")))
                .hasMessageContaining("409").hasMessageContaining("sincronizado");
        assertThat(outbox.count()).isEqualTo(antes);
    }

    @Test void rejeitaVersaoDeContratoDesconhecidaSemAlterarProjecao() {
        var valido = passageiro(803, 0, "passageiro.criado.v1", "Teste");
        var invalido = new Evento(valido.eventId(), 2, valido.eventType(), valido.aggregateId(), 0, valido.occurredAt(), valido.data());
        assertThatThrownBy(() -> passageiros.receber(invalido)).isInstanceOf(IllegalArgumentException.class);
        assertThat(projecoes.findById(803L)).isEmpty();
    }
}
