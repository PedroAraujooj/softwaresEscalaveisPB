package br.com.infnet.passagens.repositories;

import br.com.infnet.passagens.models.PassagemHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassagemHistoricoRepository extends JpaRepository<PassagemHistorico, Long> {

    boolean existsByEventId(String eventId);

    List<PassagemHistorico> findByPassagemIdOrderByRegistradoEmAscIdAsc(Long passagemId);
}
