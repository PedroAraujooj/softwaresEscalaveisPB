package br.com.infnet.passagens.dtos;

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
}
