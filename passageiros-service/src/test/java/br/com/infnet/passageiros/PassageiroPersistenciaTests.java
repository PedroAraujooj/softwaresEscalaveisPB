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
