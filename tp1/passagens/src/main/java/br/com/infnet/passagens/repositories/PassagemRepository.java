package br.com.infnet.passagens.repositories;

import br.com.infnet.passagens.models.Passagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PassagemRepository extends JpaRepository<Passagem, Long> {

    List<Passagem> findByDestinoIgnoreCase(String destino);

    boolean existsByAssentoAndOrigemIgnoreCaseAndDestinoIgnoreCaseAndData(
            Integer assento,
            String origem,
            String destino,
            LocalDate data
    );

    boolean existsByAssentoAndOrigemIgnoreCaseAndDestinoIgnoreCaseAndDataAndIdNot(
            Integer assento,
            String origem,
            String destino,
            LocalDate data,
            Long id
    );
}
