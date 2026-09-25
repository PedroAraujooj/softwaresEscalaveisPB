package br.com.infnet.eventos;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/eventos/outbox") @RequiredArgsConstructor
public class OutboxController {
    private final OutboxRepository repository;
    @GetMapping
    public Map<String, Long> status() {
        return Map.of("pendentes", repository.countByPublicadoEmIsNull(), "total", repository.count());
    }
}
