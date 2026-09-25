# Documentacao dos Microsservicos

> Documento histórico da etapa anterior, com comunicação HTTP/Feign. A implementação atual usa RabbitMQ: consulte [Arquitetura orientada a eventos](../docs/ARQUITETURA_EVENTOS.md) e o [README atual](../README.md).

## Visao Geral

O sistema foi separado em tres projetos Spring Boot independentes:

| Microsservico        | Responsabilidade |
|----------------------|--:|
| Eureka Server        | Servidor de descoberta dos microsservicos |
| Passageiros Service  | Cadastro e consulta de passageiros |
| Passagens Service    | Cadastro, consulta e historico de passagens |

Os servicos `passagens-service` e `passageiros-service` registram-se no `eureka-server`. O servico de passagens usa OpenFeign para consultar dados de passageiros pelo nome logico `passageiros-service`.


Fluxo principal:

1. O `eureka-server` sobe primeiro e disponibiliza o registro de servicos.
2. O `passageiros-service` sobe, registra-se no Eureka e expoe a API `/passageiros`.
3. O `passagens-service` sobe, registra-se no Eureka e expoe a API `/passagens`.
4. Ao criar, listar ou atualizar uma passagem, o `passagens-service` consulta o `passageiros-service` via `@FeignClient`.

Client Feign usado pelo servico de passagens:

```java
@FeignClient(name = "passageiros-service", path = "/passageiros")
public interface PassageiroClient {
    @GetMapping("/{id}")
    PassageiroResponseDTO buscarPorId(@PathVariable("id") Long id);
}
```


## Endpoints do Passageiros Service

Base URL: `http://localhost:8082`

| Metodo | Endpoint | Descricao |
|---|---|---|
| `GET` | `/passageiros` | Lista todos os passageiros |
| `POST` | `/passageiros` | Cadastra um novo passageiro |
| `GET` | `/passageiros/{id}` | Busca passageiro por ID |
| `PUT` | `/passageiros/{id}` | Atualiza passageiro por ID |
| `DELETE` | `/passageiros/{id}` | Remove passageiro por ID |
| `GET` | `/passageiros/busca?nome=Maria` | Busca passageiros pelo nome |

Exemplo de `POST /passageiros`:

```json
{
  "nome": "Ana Costa",
  "cpf": "44444444444",
  "email": "ana.costa@email.com",
  "telefone": "21999994444"
}
```

Exemplo de resposta:

```json
{
  "id": 4,
  "nome": "Ana Costa",
  "cpf": "44444444444",
  "email": "ana.costa@email.com",
  "telefone": "21999994444"
}
```

## Endpoints do Passagens Service

Base URL: `http://localhost:8081`

| Metodo | Endpoint | Descricao |
|---|---|---|
| `GET` | `/passagens` | Lista todas as passagens com dados do passageiro |
| `POST` | `/passagens` | Cadastra uma nova passagem |
| `GET` | `/passagens/{id}` | Busca passagem por ID |
| `PUT` | `/passagens/{id}` | Atualiza passagem por ID |
| `DELETE` | `/passagens/{id}` | Remove passagem por ID |
| `GET` | `/passagens/busca?destino=Sao Paulo` | Busca passagens por destino |
| `GET` | `/passagens/{id}/historico` | Lista o historico da passagem |

Exemplo de `POST /passagens`:

```json
{
  "passageiroId": 1,
  "assento": 10,
  "origem": "Rio de Janeiro",
  "destino": "Sao Paulo",
  "data": "2026-09-10",
  "status": "Reservada"
}
```

Exemplo de resposta:

```json
{
  "id": 4,
  "passageiroId": 1,
  "passageiro": {
    "id": 1,
    "nome": "Joao Silva",
    "cpf": "11111111111",
    "email": "joao.silva@email.com",
    "telefone": "21999990001"
  },
  "assento": 10,
  "origem": "Rio de Janeiro",
  "destino": "Sao Paulo",
  "data": "2026-09-10",
  "status": "Reservada"
}
```

## Regras de Negocio

- Passageiros possuem CPF e e-mail unicos.
- Passagens armazenam `passageiroId`, nao os dados completos do passageiro.
- Ao retornar uma passagem, o sistema busca os dados atualizados do passageiro via Feign.
- Nao e permitido reservar o mesmo assento para a mesma origem, destino e data.
- Operacoes de criacao, atualizacao e remocao de passagens geram registros no historico.
