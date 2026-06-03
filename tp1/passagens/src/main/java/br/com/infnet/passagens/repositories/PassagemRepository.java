package br.com.infnet.passagens.repositories;

import br.com.infnet.passagens.models.Passagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassagemRepository extends JpaRepository<Passagem, Long> {

    List<Passagem> findByDestinoIgnoreCase(String destino);

    boolean existsByAssento(Integer assento);

    boolean existsByAssentoAndIdNot(Integer assento, Long id);
}