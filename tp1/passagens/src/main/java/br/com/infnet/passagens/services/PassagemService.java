package br.com.infnet.passagens.services;

import br.com.infnet.passagens.dtos.PassagemRequestDTO;
import br.com.infnet.passagens.dtos.PassagemResponseDTO;
import br.com.infnet.passagens.models.Passagem;
import br.com.infnet.passagens.repositories.PassagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassagemService {

    private final PassagemRepository passagemRepository;

    public List<PassagemResponseDTO> listarTodas() {
        return passagemRepository.findAll()
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    public PassagemResponseDTO criar(PassagemRequestDTO requestDTO) {
        verificarAssentoExistente(requestDTO.getAssento());

        Passagem passagem = converterParaEntidade(requestDTO);
        Passagem passagemSalva = passagemRepository.save(passagem);

        return converterParaResponseDTO(passagemSalva);
    }

    public PassagemResponseDTO buscarPorId(Long id) {
        Passagem passagem = encontrarPassagemPorId(id);
        return converterParaResponseDTO(passagem);
    }

    public PassagemResponseDTO atualizar(Long id, PassagemRequestDTO requestDTO) {
        Passagem passagem = encontrarPassagemPorId(id);

        boolean assentoJaUsadoPorOutraPassagem =
                passagemRepository.existsByAssentoAndIdNot(requestDTO.getAssento(), id);

        if (assentoJaUsadoPorOutraPassagem) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assento já está reservado por outra passagem."
            );
        }

        passagem.setPassageiro(requestDTO.getPassageiro());
        passagem.setAssento(requestDTO.getAssento());
        passagem.setOrigem(requestDTO.getOrigem());
        passagem.setDestino(requestDTO.getDestino());
        passagem.setData(requestDTO.getData());
        passagem.setStatus(requestDTO.getStatus());

        Passagem passagemAtualizada = passagemRepository.save(passagem);

        return converterParaResponseDTO(passagemAtualizada);
    }

    public void deletar(Long id) {
        Passagem passagem = encontrarPassagemPorId(id);
        passagemRepository.delete(passagem);
    }

    public List<PassagemResponseDTO> buscarPorDestino(String destino) {
        return passagemRepository.findByDestinoIgnoreCase(destino)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    private Passagem encontrarPassagemPorId(Long id) {
        return passagemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Passagem não encontrada."
                ));
    }

    private void verificarAssentoExistente(Integer assento) {
        boolean existe = passagemRepository.existsByAssento(assento);

        if (existe) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assento já está reservado."
            );
        }
    }

    private Passagem converterParaEntidade(PassagemRequestDTO requestDTO) {
        Passagem passagem = new Passagem();

        passagem.setPassageiro(requestDTO.getPassageiro());
        passagem.setAssento(requestDTO.getAssento());
        passagem.setOrigem(requestDTO.getOrigem());
        passagem.setDestino(requestDTO.getDestino());
        passagem.setData(requestDTO.getData());
        passagem.setStatus(requestDTO.getStatus());

        return passagem;
    }

    private PassagemResponseDTO converterParaResponseDTO(Passagem passagem) {
        return new PassagemResponseDTO(
                passagem.getId(),
                passagem.getPassageiro(),
                passagem.getAssento(),
                passagem.getOrigem(),
                passagem.getDestino(),
                passagem.getData(),
                passagem.getStatus()
        );
    }
}