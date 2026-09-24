package br.com.infnet.passagens.clients;

import br.com.infnet.passagens.dtos.PassageiroResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "passageiros-service", path = "/passageiros")
public interface PassageiroClient {

    @GetMapping("/{id}")
    PassageiroResponseDTO buscarPorId(@PathVariable("id") Long id);
}
