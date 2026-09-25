package br.com.infnet.passagens.eventos;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/eventos") @RequiredArgsConstructor
public class EventosController {
    private final NotificacaoRepository notificacoes;
    private final PassageiroProjecaoRepository passageiros;
    @GetMapping("/notificacoes")
    public List<Notificacao> notificacoes() { return notificacoes.findAll(); }
    @GetMapping("/passageiros/{id}")
    public PassageiroProjecao passageiro(@PathVariable Long id) {
        return passageiros.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Projecao ainda nao disponivel"));
    }
}
