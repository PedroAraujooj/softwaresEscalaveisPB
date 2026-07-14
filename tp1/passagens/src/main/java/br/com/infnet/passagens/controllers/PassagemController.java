package br.com.infnet.passagens.controllers;

import br.com.infnet.passagens.dtos.PassagemHistoricoResponseDTO;
import br.com.infnet.passagens.dtos.PassagemRequestDTO;
import br.com.infnet.passagens.dtos.PassagemResponseDTO;
import br.com.infnet.passagens.services.PassagemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passagens")
@RequiredArgsConstructor
public class PassagemController {

    private final PassagemService passagemService;

    @GetMapping
    public List<PassagemResponseDTO> listarTodas() {
        return passagemService.listarTodas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PassagemResponseDTO criar(@RequestBody PassagemRequestDTO requestDTO) {
        return passagemService.criar(requestDTO);
    }

    @GetMapping("/{id}")
    public PassagemResponseDTO buscarPorId(@PathVariable Long id) {
        return passagemService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public PassagemResponseDTO atualizar(
            @PathVariable Long id,
            @RequestBody PassagemRequestDTO requestDTO
    ) {
        return passagemService.atualizar(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        passagemService.deletar(id);
    }

    @GetMapping("/busca")
    public List<PassagemResponseDTO> buscarPorDestino(@RequestParam String destino) {
        return passagemService.buscarPorDestino(destino);
    }

    @GetMapping("/{id}/historico")
    public List<PassagemHistoricoResponseDTO> listarHistorico(@PathVariable Long id) {
        return passagemService.listarHistorico(id);
    }
}
