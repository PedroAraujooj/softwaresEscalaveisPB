# Frontend simples - Passagens

Frontend em React + Vite para consumir a API Spring Boot de passagens.

## Endpoints consumidos

- `GET /passagens` - lista todas as passagens
- `POST /passagens` - cria uma passagem
- `GET /passagens/busca?destino=...` - busca por destino
- `PUT /passagens/{id}` - atualiza uma passagem
- `DELETE /passagens/{id}` - deleta uma passagem

## Campos usados

```json
{
  "passageiro": "João",
  "assento": 10,
  "origem": "Vitória",
  "destino": "Rio de Janeiro",
  "data": "2026-06-01",
  "status": "RESERVADA"
}
```

## Como rodar

Instale as dependências:

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

## Configuração da URL da API

Por padrão, o frontend chama:

```text
http://localhost:8080
```

Se sua API estiver em outra porta, crie um arquivo `.env` na raiz do projeto:

```env
VITE_API_URL=http://localhost:8080
```

## Observação sobre CORS

Se aparecer erro de CORS no navegador, adicione no controller Spring:

```java
@CrossOrigin(origins = "http://localhost:5173")
```

Exemplo:

```java
@RestController
@RequestMapping("/passagens")
@CrossOrigin(origins = "http://localhost:5173")
public class PassagemController {
    // ...
}
```


## Compatibilidade com Node 20.11.0

Este projeto está fixado com `vite@5.4.19`, compatível com Node 20.11.0.

Se você preferir usar o Vite mais novo, atualize o Node para 20.19+ ou 22.12+.
