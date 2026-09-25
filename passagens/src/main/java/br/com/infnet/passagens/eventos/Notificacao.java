package br.com.infnet.passagens.eventos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Getter @Setter
public class Notificacao {
    @Id
    private String eventId;
    private Long passagemId;
    private String mensagem;
    private Instant registradaEm;
}
