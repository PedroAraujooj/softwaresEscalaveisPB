package br.com.infnet.passagens.eventos;

import br.com.infnet.passagens.dtos.PassageiroResponseDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "passageiros_projecao") @Getter @Setter
public class PassageiroProjecao {
    @Id
    private Long id;
    @Version
    private Long lockVersion;
    private long eventVersion = -1;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private boolean removido;

    public PassageiroResponseDTO toDTO() {
        return new PassageiroResponseDTO(id, nome, cpf, email, telefone);
    }
}
