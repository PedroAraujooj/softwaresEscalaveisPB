package br.com.infnet.passagens.services;

import br.com.infnet.passagens.dtos.PassagemRequestDTO;
import br.com.infnet.passagens.dtos.PassagemHistoricoResponseDTO;
import br.com.infnet.passagens.dtos.PassagemResponseDTO;
import br.com.infnet.passagens.models.OperacaoHistorico;
import br.com.infnet.passagens.models.Passagem;
import br.com.infnet.passagens.models.PassagemHistorico;
import br.com.infnet.passagens.repositories.PassagemHistoricoRepository;
import br.com.infnet.passagens.repositories.PassagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassagemService {

    private final PassagemRepository passagemRepository;
    private final PassagemHistoricoRepository passagemHistoricoRepository;

    @Transactional(readOnly = true)
    public List<PassagemResponseDTO> listarTodas() {
        return passagemRepository.findAll()
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional
    public PassagemResponseDTO criar(PassagemRequestDTO requestDTO) {
        verificarAssentoDisponivelParaViagem(requestDTO);

        Passagem passagem = converterParaEntidade(requestDTO);
        Passagem passagemSalva = passagemRepository.save(passagem);
        registrarHistorico(passagemSalva, OperacaoHistorico.CRIACAO);

        return converterParaResponseDTO(passagemSalva);
    }

    @Transactional(readOnly = true)
    public PassagemResponseDTO buscarPorId(Long id) {
        Passagem passagem = encontrarPassagemPorId(id);
        return converterParaResponseDTO(passagem);
    }

    @Transactional
    public PassagemResponseDTO atualizar(Long id, PassagemRequestDTO requestDTO) {
        Passagem passagem = encontrarPassagemPorId(id);

        boolean assentoJaUsadoPorOutraPassagem =
                passagemRepository.existsByAssentoAndOrigemIgnoreCaseAndDestinoIgnoreCaseAndDataAndIdNot(
                        requestDTO.getAssento(),
                        requestDTO.getOrigem(),
                        requestDTO.getDestino(),
                        requestDTO.getData(),
                        id
                );

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
        registrarHistorico(passagemAtualizada, OperacaoHistorico.ATUALIZACAO);

        return converterParaResponseDTO(passagemAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        Passagem passagem = encontrarPassagemPorId(id);
        registrarHistorico(passagem, OperacaoHistorico.REMOCAO);
        passagemRepository.delete(passagem);
    }

    @Transactional(readOnly = true)
    public List<PassagemResponseDTO> buscarPorDestino(String destino) {
        return passagemRepository.findByDestinoIgnoreCase(destino)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PassagemHistoricoResponseDTO> listarHistorico(Long passagemId) {
        return passagemHistoricoRepository.findByPassagemIdOrderByRegistradoEmAscIdAsc(passagemId)
                .stream()
                .map(PassagemHistoricoResponseDTO::de)
                .toList();
    }

    private Passagem encontrarPassagemPorId(Long id) {
        return passagemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Passagem não encontrada."
                ));
    }

    private void verificarAssentoDisponivelParaViagem(PassagemRequestDTO requestDTO) {
        boolean existe = passagemRepository.existsByAssentoAndOrigemIgnoreCaseAndDestinoIgnoreCaseAndData(
                requestDTO.getAssento(),
                requestDTO.getOrigem(),
                requestDTO.getDestino(),
                requestDTO.getData()
        );

        if (existe) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assento já está reservado."
            );
        }
    }

    private void registrarHistorico(Passagem passagem, OperacaoHistorico operacao) {
        passagemHistoricoRepository.save(PassagemHistorico.registrar(passagem, operacao));
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
