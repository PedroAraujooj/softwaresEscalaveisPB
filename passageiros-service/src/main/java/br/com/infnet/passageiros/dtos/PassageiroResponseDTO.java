package br.com.infnet.passageiros.dtos;

import br.com.infnet.passageiros.models.Passageiro;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassageiroResponseDTO {

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;

    public static PassageiroResponseDTO de(Passageiro passageiro) {
        return new PassageiroResponseDTO(
                passageiro.getId(),
                passageiro.getNome(),
                passageiro.getCpf(),
                passageiro.getEmail(),
                passageiro.getTelefone()
        );
    }
}
