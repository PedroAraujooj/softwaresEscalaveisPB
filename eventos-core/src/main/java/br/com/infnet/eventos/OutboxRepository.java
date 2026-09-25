package br.com.infnet.eventos;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Outbox> findTop20ByPublicadoEmIsNullOrderByIdAsc();
    long countByPublicadoEmIsNull();
}
