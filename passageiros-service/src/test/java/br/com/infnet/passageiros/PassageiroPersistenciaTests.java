package br.com.infnet.passageiros;

import br.com.infnet.passageiros.dtos.PassageiroRequestDTO;
import br.com.infnet.passageiros.repositories.PassageiroRepository;
import br.com.infnet.passageiros.services.PassageiroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "eureka.client.enabled=false")
@Transactional
class PassageiroPersistenciaTests {

    @Autowired
    private PassageiroService passageiroService;

    @Autowired
    private PassageiroRepository passageiroRepository;

    @Autowired
    private br.com.infnet.eventos.OutboxRepository outbox;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper mapper;

    @Test
    void publicaSnapshotsVersionadosDeCriacaoAtualizacaoERemocao() throws Exception {
        var request = new PassageiroRequestDTO("Evento teste", "98765432101", "evento@example.com", "21999998888");
        var criado = passageiroService.criar(request);
        passageiroService.atualizar(criado.getId(), new PassageiroRequestDTO(
                "Nome atualizado", request.getCpf(), request.getEmail(), request.getTelefone()));
        passageiroService.deletar(criado.getId());
        var itens = outbox.findAll(org.springframework.data.domain.Sort.by("id"));
        assertThat(itens).hasSize(3);
        assertThat(itens).extracting(br.com.infnet.eventos.Outbox::getRoutingKey)
                .containsExactly("passageiro.criado.v1", "passageiro.atualizado.v1", "passageiro.removido.v1");
        for (int i = 0; i < itens.size(); i++) {
            var evento = mapper.readValue(itens.get(i).getPayload(), br.com.infnet.eventos.Evento.class);
            assertThat(evento.aggregateVersion()).isEqualTo(i);
            assertThat(evento.aggregateId()).isEqualTo(criado.getId().toString());
            assertThat(itens.get(i).getPublicadoEm()).isNull();
        }
        assertThat(passageiroRepository.findById(criado.getId())).isEmpty();
    }

    @Test
    void devePersistirPassageiro() {
        var passageiro = passageiroService.criar(new PassageiroRequestDTO(
                "Ana Costa",
                "44444444444",
                "ana.costa@email.com",
                "21999994444"
        ));

        assertThat(passageiroRepository.findById(passageiro.getId())).isPresent();
        assertThat(passageiro.getNome()).isEqualTo("Ana Costa");
    }

    @Test
    void naoDevePermitirCpfDuplicado() {
        passageiroService.criar(new PassageiroRequestDTO(
                "Bruno Lima",
                "55555555555",
                "bruno.lima@email.com",
                "21999995555"
        ));

        assertThatThrownBy(() -> passageiroService.criar(new PassageiroRequestDTO(
                "Bruna Lima",
                "55555555555",
                "bruna.lima@email.com",
                "21999996666"
        ))).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveBuscarPorNome() {
        var encontrados = passageiroService.buscarPorNome("Maria");

        assertThat(encontrados)
                .extracting("nome")
                .contains("Maria Souza");
    }
}
