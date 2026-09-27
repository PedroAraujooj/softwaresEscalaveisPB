# Sistema de passagens

Este projeto é um trabalho da disciplina de Software Escaláveis. Ele permite cadastrar passageiros e gerenciar passagens, usando microsserviços que se comunicam por mensagens.

## O que o sistema faz

- Cadastra, consulta, atualiza e exclui passageiros e passagens.
- Verifica regras como CPF duplicado e assento já reservado na mesma viagem.
- Guarda o histórico de criação, atualização e exclusão das passagens.
- Gera notificações simuladas quando uma passagem é criada.

## Como o projeto está organizado

O `passageiros-service` cuida dos passageiros e o `passagens` cuida das passagens. Cada serviço tem seu próprio banco PostgreSQL. O frontend foi feito com React e Vite.

A comunicação entre os serviços usa RabbitMQ. Quando um passageiro é cadastrado ou alterado, um evento atualiza seus dados no serviço de passagens. O histórico e as notificações também são processados por eventos, então podem levar alguns instantes para aparecer.

O módulo `eventos-core` reúne o código compartilhado de mensagens e da outbox. A outbox salva o evento no banco antes do envio, permitindo tentar novamente se o RabbitMQ estiver indisponível.

O `eureka-server` foi usado em uma etapa anterior e continua no repositório, mas não participa da execução atual. No Kubernetes, a descoberta dos serviços é feita pelos nomes dos Services.

## Tecnologias e operação

- **Java 21 e Spring Boot:** desenvolvimento das APIs.
- **PostgreSQL:** armazenamento dos dados. Os testes Java usam H2.
- **Docker e Docker Compose:** execução do sistema em contêineres.
- **Kubernetes:** implantação, réplicas e reposição de pods. Os arquivos estão em `k8s/`.
- **Fluent Bit, Loki e Grafana:** coleta e consulta dos logs.
- **Micrometer Tracing e Zipkin:** acompanhamento do caminho dos eventos entre os serviços.
- **Spring Boot Actuator:** verificação da saúde das APIs.

## Como executar

Com o Docker Desktop aberto e usando contêineres Linux, execute na pasta principal:

```powershell
docker compose up -d --build --wait --wait-timeout 300
```

A primeira execução pode demorar por causa do download das imagens e da compilação.

| Acesso pelo Docker Compose | Endereço |
|---|---|
| Frontend | http://localhost:5173 |
| API de passageiros | http://localhost:8082/passageiros |
| API de passagens | http://localhost:8081/passagens |
| Saúde de passageiros | http://localhost:8082/actuator/health/readiness |
| Saúde de passagens | http://localhost:8081/actuator/health/readiness |
| RabbitMQ | http://localhost:15672 — `app` / `app-local` |
| Grafana | http://localhost:3000 — `admin` / `admin-local` |
| Zipkin | http://localhost:9411 |

O histórico é consultado em `GET /passagens/{id}/historico`, usando o ID da passagem. Ele continua disponível depois da exclusão da passagem.

As senhas acima são do ambiente local de estudo. Para parar os contêineres mantendo os dados, use `docker compose down`.

## Testes e CI/CD

Os testes Java verificam as regras do sistema, a persistência dos dados e o processamento de eventos. Com Maven e JDK 21 instalados, execute:

```powershell
mvn verify
```

A pasta `postman/` contém a coleção para testar as APIs manualmente e o environment do Kubernetes. Nesse environment, as portas são 18082 para passageiros e 18081 para passagens, com os respectivos `port-forward` abertos.

O GitHub Actions executa os testes Java, valida os arquivos Kubernetes e inicia o ambiente Docker. Depois, faz verificações básicas de saúde e listagem das APIs com `curl` e `jq`. A coleção Postman não roda no CI.

Após um push em `main` ou `master` com os testes aprovados, o workflow publica as imagens no GHCR. A implantação automática no Kubernetes depende da configuração do runner, dos secrets e da variável `ENABLE_K8S_DEPLOY`.
