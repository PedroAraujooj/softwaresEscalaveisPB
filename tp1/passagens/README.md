# Passagens

API REST para gerenciamento de passagens, desenvolvida com Spring Boot, Spring Data JPA e banco H2 em memoria. O projeto permite cadastrar, consultar, atualizar, remover e buscar passagens por destino, alem de manter um historico das alteracoes realizadas em cada passagem.

## Tecnologias

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- H2 Database
- Maven
- Lombok

## Funcionalidades

- CRUD de passagens.
- Busca de passagens por destino.
- Validacao de assento unico.
- Persistencia real com JPA/Hibernate.
- Historico de criacao, atualizacao e remocao de passagens.
- Testes automatizados da camada de persistencia.

## Endpoints principais

| Metodo | Endpoint | Descricao |
|---|---|---|
| `GET` | `/passagens` | Lista todas as passagens |
| `POST` | `/passagens` | Cria uma nova passagem |
| `GET` | `/passagens/{id}` | Busca uma passagem por ID |
| `PUT` | `/passagens/{id}` | Atualiza uma passagem |
| `DELETE` | `/passagens/{id}` | Remove uma passagem |
| `GET` | `/passagens/busca?destino=...` | Busca passagens por destino |
| `GET` | `/passagens/{id}/historico` | Lista o historico de uma passagem |


## Sequencia: criar passagem

```mermaid
sequenceDiagram
    actor Cliente
    participant Controller as PassagemController
    participant Service as PassagemService
    participant PassagemRepo as PassagemRepository
    participant HistoricoRepo as PassagemHistoricoRepository
    participant DB as H2

    Cliente->>Controller: POST /passagens
    Controller->>Service: criar(request)
    Service->>PassagemRepo: existsByAssento(assento)
    PassagemRepo->>DB: consulta assento
    DB-->>PassagemRepo: resultado
    PassagemRepo-->>Service: assento disponivel
    Service->>PassagemRepo: save(passagem)
    PassagemRepo->>DB: INSERT passagens
    DB-->>PassagemRepo: passagem salva
    PassagemRepo-->>Service: Passagem
    Service->>HistoricoRepo: save(CRIACAO)
    HistoricoRepo->>DB: INSERT passagens_historico
    Service-->>Controller: PassagemResponseDTO
    Controller-->>Cliente: 201 Created
```

## Sequencia: atualizar passagem

```mermaid
sequenceDiagram
    actor Cliente
    participant Controller as PassagemController
    participant Service as PassagemService
    participant PassagemRepo as PassagemRepository
    participant HistoricoRepo as PassagemHistoricoRepository
    participant DB as H2

    Cliente->>Controller: PUT /passagens/{id}
    Controller->>Service: atualizar(id, request)
    Service->>PassagemRepo: findById(id)
    PassagemRepo->>DB: SELECT passagem
    DB-->>PassagemRepo: passagem encontrada
    Service->>PassagemRepo: existsByAssentoAndIdNot(assento, id)
    PassagemRepo->>DB: consulta conflito de assento
    DB-->>PassagemRepo: sem conflito
    Service->>PassagemRepo: save(passagem atualizada)
    PassagemRepo->>DB: UPDATE passagens
    Service->>HistoricoRepo: save(ATUALIZACAO)
    HistoricoRepo->>DB: INSERT passagens_historico
    Service-->>Controller: PassagemResponseDTO
    Controller-->>Cliente: 200 OK
```

## Como executar

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicacao sobe em `http://localhost:8080`.

Console H2:

```text
http://localhost:8080/h2-console
```

Dados de conexao:

```text
JDBC URL: jdbc:h2:mem:passagensdb
User: sa
Password:
```

## Como testar

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
.\mvnw.cmd test
```

Os testes validam o carregamento da aplicacao e o comportamento de persistencia com registro de historico.
