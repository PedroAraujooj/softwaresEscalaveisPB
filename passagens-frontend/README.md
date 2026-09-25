# Frontend simples - Passagens e Passageiros

Frontend em React + Vite para consumir os microsservicos Spring Boot de passagens e passageiros.

## Endpoints consumidos

### Passagens

- `GET /passagens` - lista todas as passagens
- `POST /passagens` - cria uma passagem
- `GET /passagens/busca?destino=...` - busca por destino
- `PUT /passagens/{id}` - atualiza uma passagem
- `DELETE /passagens/{id}` - deleta uma passagem

### Passageiros

- `GET /passageiros` - lista todos os passageiros
- `POST /passageiros` - cria um passageiro
- `GET /passageiros/busca?nome=...` - busca por nome
- `PUT /passageiros/{id}` - atualiza um passageiro
- `DELETE /passageiros/{id}` - deleta um passageiro

## Campos usados

### Passagem

```json
{
  "passageiroId": 1,
  "assento": 10,
  "origem": "Vitoria",
  "destino": "Rio de Janeiro",
  "data": "2026-06-01",
  "status": "RESERVADA"
}
```

### Passageiro

```json
{
  "nome": "Joao",
  "cpf": "12345678900",
  "email": "joao@email.com",
  "telefone": "27999999999"
}
```

## Como rodar

### Com Docker Compose

Na raiz do repositório, execute:

```bash
docker compose up -d --build
```

O frontend sobe junto com os demais serviços em http://localhost:5173. O Nginx serve os arquivos compilados e encaminha as chamadas às APIs pela rede Docker. Após alterações no frontend, execute novamente o comando com `--build`.

### Desenvolvimento local

Se o frontend do Docker estiver rodando, execute `docker compose stop frontend` na raiz para liberar a porta 5173. Depois, nesta pasta:

Instale as dependencias:

```bash
npm install
```

Rode o projeto:

```bash
npm run dev
```

Acesse no navegador:

```text
http://localhost:5173
```

## Configuracao da URL das APIs

No desenvolvimento local, o frontend chama:

```text
http://localhost:8081
http://localhost:8082
```

Se sua API estiver em outra porta, crie um arquivo `.env` na raiz do projeto:

```env
VITE_PASSAGENS_API_URL=http://localhost:8081
VITE_PASSAGEIROS_API_URL=http://localhost:8082
```

No Docker, o build usa `/api` como base para ambas as APIs, com encaminhamento pelo Nginx.

## Observacao sobre CORS

Os microsservicos ja estao configurados para aceitar chamadas do React/Vite.

## Compatibilidade com Node 20.11.0

Este projeto esta fixado com `vite@5.4.19`, compativel com Node 20.11.0.

Se voce preferir usar o Vite mais novo, atualize o Node para 20.19+ ou 22.12+.
