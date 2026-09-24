package br.com.infnet.passageiros.services;

import br.com.infnet.passageiros.dtos.PassageiroRequestDTO;
import br.com.infnet.passageiros.dtos.PassageiroResponseDTO;
import br.com.infnet.passageiros.models.Passageiro;
import br.com.infnet.passageiros.repositories.PassageiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassageiroService {

    private final PassageiroRepository passageiroRepository;

    @Transactional(readOnly = true)
    public List<PassageiroResponseDTO> listarTodos() {
        return passageiroRepository.findAll()
                .stream()
                .map(PassageiroResponseDTO::de)
                .toList();
    }

    @Transactional
    public PassageiroResponseDTO criar(PassageiroRequestDTO requestDTO) {
        validarCpfEmailDisponiveis(requestDTO);

        Passageiro passageiro = converterParaEntidade(requestDTO);
        return PassageiroResponseDTO.de(passageiroRepository.save(passageiro));
    }

    @Transactional(readOnly = true)
    public PassageiroResponseDTO buscarPorId(Long id) {
        return PassageiroResponseDTO.de(encontrarPorId(id));
    }

    @Transactional
    public PassageiroResponseDTO atualizar(Long id, PassageiroRequestDTO requestDTO) {
        Passageiro passageiro = encontrarPorId(id);

        if (passageiroRepository.existsByCpfAndIdNot(requestDTO.getCpf(), id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF ja cadastrado por outro passageiro.");
        }

        if (passageiroRepository.existsByEmailIgnoreCaseAndIdNot(requestDTO.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail ja cadastrado por outro passageiro.");
        }

        passageiro.setNome(requestDTO.getNome());
        passageiro.setCpf(requestDTO.getCpf());
        passageiro.setEmail(requestDTO.getEmail());
        passageiro.setTelefone(requestDTO.getTelefone());

        return PassageiroResponseDTO.de(passageiroRepository.save(passageiro));
    }

    @Transactional
    public void deletar(Long id) {
        Passageiro passageiro = encontrarPorId(id);
        passageiroRepository.delete(passageiro);
    }

    @Transactional(readOnly = true)
    public List<PassageiroResponseDTO> buscarPorNome(String nome) {
        return passageiroRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(PassageiroResponseDTO::de)
                .toList();
    }

    private Passageiro encontrarPorId(Long id) {
        return passageiroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passageiro nao encontrado."));
    }

    private void validarCpfEmailDisponiveis(PassageiroRequestDTO requestDTO) {
        if (passageiroRepository.existsByCpf(requestDTO.getCpf())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF ja cadastrado.");
        }

        if (passageiroRepository.existsByEmailIgnoreCase(requestDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail ja cadastrado.");
        }
    }

    private Passageiro converterParaEntidade(PassageiroRequestDTO requestDTO) {
        Passageiro passageiro = new Passageiro();
        passageiro.setNome(requestDTO.getNome());
        passageiro.setCpf(requestDTO.getCpf());
        passageiro.setEmail(requestDTO.getEmail());
        passageiro.setTelefone(requestDTO.getTelefone());
        return passageiro;
    }
}
