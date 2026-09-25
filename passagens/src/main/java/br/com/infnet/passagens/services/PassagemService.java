package br.com.infnet.passagens.services;

import br.com.infnet.passagens.dtos.PassageiroResponseDTO;
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
    private final br.com.infnet.passagens.eventos.PassageiroProjecaoRepository projecaoRepository;
    private final br.com.infnet.eventos.Eventos eventos;

    @Transactional(readOnly = true)
    public List<PassagemResponseDTO> listarTodas() {
        return passagemRepository.findAll()
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional
    public PassagemResponseDTO criar(PassagemRequestDTO requestDTO) {
        PassageiroResponseDTO passageiro = buscarPassageiroAtivo(requestDTO.getPassageiroId());
        verificarAssentoDisponivelParaViagem(requestDTO);

        Passagem passagem = converterParaEntidade(requestDTO);
        Passagem passagemSalva = passagemRepository.save(passagem);
        registrarHistorico(passagemSalva, OperacaoHistorico.CRIACAO, passageiro.getNome());

        return converterParaResponseDTO(passagemSalva, passageiro);
    }

    @Transactional(readOnly = true)
    public PassagemResponseDTO buscarPorId(Long id) {
        Passagem passagem = encontrarPassagemPorId(id);
        return converterParaResponseDTO(passagem);
    }

    @Transactional
    public PassagemResponseDTO atualizar(Long id, PassagemRequestDTO requestDTO) {
        Passagem passagem = encontrarPassagemPorId(id);
        PassageiroResponseDTO passageiro = buscarPassageiroAtivo(requestDTO.getPassageiroId());

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

        passagem.setPassageiroId(requestDTO.getPassageiroId());
        passagem.setAssento(requestDTO.getAssento());
        passagem.setOrigem(requestDTO.getOrigem());
        passagem.setDestino(requestDTO.getDestino());
        passagem.setData(requestDTO.getData());
        passagem.setStatus(requestDTO.getStatus());

        Passagem passagemAtualizada = passagemRepository.save(passagem);
        registrarHistorico(passagemAtualizada, OperacaoHistorico.ATUALIZACAO, passageiro.getNome());

        return converterParaResponseDTO(passagemAtualizada, passageiro);
    }

    @Transactional
    public void deletar(Long id) {
        Passagem passagem = encontrarPassagemPorId(id);
        PassageiroResponseDTO passageiro = buscarPassageiroPorId(passagem.getPassageiroId());
        registrarHistorico(passagem, OperacaoHistorico.REMOCAO, passageiro.getNome());
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

    private void registrarHistorico(Passagem passagem, OperacaoHistorico operacao, String passageiroNome) {
        String tipo = switch (operacao) {
            case CRIACAO -> "passagem.criada.v1";
            case ATUALIZACAO -> "passagem.atualizada.v1";
            case REMOCAO -> "passagem.removida.v1";
        };
        eventos.registrar(tipo, passagem.getId(), 0, PassagemHistorico.registrar(passagem, operacao, passageiroNome));
    }

    private PassageiroResponseDTO buscarPassageiroAtivo(Long id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o passageiro.");
        var projecao = projecaoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT, "Passageiro ainda nao sincronizado. Aguarde alguns instantes e tente novamente."));
        if (projecao.isRemovido()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passageiro removido.");
        return projecao.toDTO();
    }

    private PassageiroResponseDTO buscarPassageiroPorId(Long id) {
        return projecaoRepository.findById(id).map(br.com.infnet.passagens.eventos.PassageiroProjecao::toDTO)
                .orElseGet(() -> new PassageiroResponseDTO(id, "Aguardando sincronizacao", null, null, null));
    }

    private Passagem converterParaEntidade(PassagemRequestDTO requestDTO) {
        Passagem passagem = new Passagem();

        passagem.setPassageiroId(requestDTO.getPassageiroId());
        passagem.setAssento(requestDTO.getAssento());
        passagem.setOrigem(requestDTO.getOrigem());
        passagem.setDestino(requestDTO.getDestino());
        passagem.setData(requestDTO.getData());
        passagem.setStatus(requestDTO.getStatus());

        return passagem;
    }

    private PassagemResponseDTO converterParaResponseDTO(Passagem passagem) {
        return converterParaResponseDTO(passagem, buscarPassageiroPorId(passagem.getPassageiroId()));
    }

    private PassagemResponseDTO converterParaResponseDTO(Passagem passagem, PassageiroResponseDTO passageiro) {
        return new PassagemResponseDTO(
                passagem.getId(),
                passagem.getPassageiroId(),
                passageiro,
                passagem.getAssento(),
                passagem.getOrigem(),
                passagem.getDestino(),
                passagem.getData(),
                passagem.getStatus()
        );
    }
}
