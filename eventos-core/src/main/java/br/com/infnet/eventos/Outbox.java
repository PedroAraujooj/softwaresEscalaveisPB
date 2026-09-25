package br.com.infnet.eventos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "outbox", indexes = @Index(name = "idx_outbox_pendente", columnList = "publicadoEm,id"))
@Getter @Setter
public class Outbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String eventId;
    @Column(nullable = false)
    private String routingKey;
    @Column(nullable = false, columnDefinition = "text")
    private String payload;
    private Instant publicadoEm;
    private int tentativas;
}
