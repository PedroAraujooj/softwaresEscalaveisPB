package br.com.infnet.passageiros.controllers;

import br.com.infnet.passageiros.dtos.PassageiroRequestDTO;
import br.com.infnet.passageiros.dtos.PassageiroResponseDTO;
import br.com.infnet.passageiros.services.PassageiroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/passageiros")
@RequiredArgsConstructor
public class PassageiroController {

    private final PassageiroService passageiroService;

    @GetMapping
    public List<PassageiroResponseDTO> listarTodos() {
        return passageiroService.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PassageiroResponseDTO criar(@RequestBody PassageiroRequestDTO requestDTO) {
        return passageiroService.criar(requestDTO);
    }

    @GetMapping("/{id}")
    public PassageiroResponseDTO buscarPorId(@PathVariable Long id) {
        return passageiroService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public PassageiroResponseDTO atualizar(
            @PathVariable Long id,
            @RequestBody PassageiroRequestDTO requestDTO
    ) {
        return passageiroService.atualizar(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        passageiroService.deletar(id);
    }

    @GetMapping("/busca")
    public List<PassageiroResponseDTO> buscarPorNome(@RequestParam String nome) {
        return passageiroService.buscarPorNome(nome);
    }
}
