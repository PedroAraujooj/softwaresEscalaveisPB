package br.com.infnet.passageiros.repositories;

import br.com.infnet.passageiros.models.Passageiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassageiroRepository extends JpaRepository<Passageiro, Long> {

    List<Passageiro> findByNomeContainingIgnoreCase(String nome);

    boolean existsByCpf(String cpf);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
