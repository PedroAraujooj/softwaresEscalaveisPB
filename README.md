# Sistema de passagens — arquitetura orientada a eventos

Projeto da etapa de Software Escaláveis: Spring Boot 3.5, Java 21, Spring AMQP e RabbitMQ. O serviço de passagens usa uma projeção local de passageiros; histórico e notificações simuladas são processados por eventos. Não há mais chamada Feign entre os serviços.

## Executar

Pré-requisito: Docker Desktop com containers Linux, Docker Compose e portas 5173, 8081, 8082, 5672 e 15672 disponíveis. Se estiver executando `npm run dev`, encerre-o com `Ctrl+C` para liberar a porta 5173.

```powershell
docker compose up -d --build
docker compose logs -f passageiros passagens
```

A primeira execução baixa as imagens e compila os projetos. Aguarde os logs `Started PassagensApplication` e `Started PassageirosApplication`. O ambiente inicia vazio: a demonstração cria seus próprios dados.

| Acesso | Endereço |
|---|---|
| Frontend | http://localhost:5173 |
| Passagens | http://localhost:8081/passagens |
| Passageiros | http://localhost:8082/passageiros |
| RabbitMQ Management | http://localhost:15672 — usuário `app`, senha `app-local` |
| Outbox de cada serviço | http://localhost:8081/eventos/outbox e http://localhost:8082/eventos/outbox |
| Notificações simuladas | http://localhost:8081/eventos/notificacoes |

O Compose inicia também o frontend, compilado com Vite e servido pelo Nginx. As chamadas `/api/passagens` e `/api/passageiros` são encaminhadas aos serviços pela rede Docker. Não é necessário executar `npm run dev` para usar esse ambiente.

O Compose usa PostgreSQL separado por serviço e volumes persistentes. As credenciais são apenas para desenvolvimento local. Para parar sem apagar os dados: `docker compose down`.


## Testes e desenvolvimento

Com JDK 21 e Maven 3.9+:

```powershell
mvn test
mvn install -DskipTests
docker compose up -d rabbitmq
# Em terminais separados, sem os containers das APIs usando as mesmas portas:
mvn -f passageiros-service/pom.xml spring-boot:run
mvn -f passagens/pom.xml spring-boot:run
```

Fora do Compose, os bancos H2 ficam em arquivos `data/` no diretório de execução. Os testes usam H2 em memória e consumidores/publisher desligados; os cenários reais de RabbitMQ são exercitados pelo script. O Eureka foi preservado para a etapa anterior, mas está desativado por padrão e não participa da comunicação por eventos.

Para desenvolver o frontend fora do Docker, pare apenas seu container (`docker compose stop frontend`) e execute:

```powershell
cd passagens-frontend
npm install
npm run dev
```


O código de infraestrutura compartilhado está em `eventos-core`; cada serviço continua sendo uma aplicação executável com banco próprio. O módulo compartilhado não contém entidades de domínio dos serviços.
