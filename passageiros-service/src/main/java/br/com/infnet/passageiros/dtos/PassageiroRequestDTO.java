package br.com.infnet.passageiros.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassageiroRequestDTO {

    private String nome;
    private String cpf;
    private String email;
    private String telefone;
}
