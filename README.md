# DozenFlow — Backend

API REST para o [DozenFlow](https://dozenflow.netlify.app), um quadro Kanban
para gerenciamento de tarefas. Este repositório contém apenas o backend; o
frontend Angular vive em [`proj-dozenflow-fe`](https://github.com/ricardotecpro/proj-dozenflow-fe).

## Stack

- Java 21
- Spring Boot 3 (Web, Data JPA, Validation, Security)
- springdoc-openapi (Swagger UI)
- H2 (desenvolvimento) / PostgreSQL (produção)
- Maven

## Pré-requisitos

- JDK 21+ (o projeto inclui um `.tool-versions` — se você usa [asdf](https://asdf-vm.com/), `asdf install` resolve isso automaticamente)
- Maven (ou use o wrapper `./mvnw` incluído, não requer instalação)

## Como rodar (desenvolvimento)

```bash
./mvnw spring-boot:run
```

Por padrão a aplicação sobe com o profile `dev` (`spring.profiles.active=dev`
em `application.properties`), usando banco H2 em memória. A API fica
disponível em `http://localhost:8080`.

- Console H2: `http://localhost:8080/h2-console`
  (JDBC URL: `jdbc:h2:mem:dozendb`, usuário `sa`, sem senha)
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`

### Popular dados de teste (seed)

Por padrão o banco H2 de desenvolvimento sobe vazio (é assim que os testes
esperam encontrá-lo). Para testar manualmente com dados de exemplo — útil
pra QA visual do board —, ative o profile `seed` junto do `dev`:

```bash
SPRING_PROFILES_ACTIVE=dev,seed ./mvnw spring-boot:run
```

Isso popula a tabela `tasks` com tarefas de exemplo nas três colunas (veja
`src/main/resources/data-seed.sql`). Nunca ativa em testes ou em produção.

## Como rodar em produção

Produção usa o profile `prod`, que espera um PostgreSQL real e **não**
expõe o H2 console. Configure as variáveis de ambiente abaixo (veja também
`src/main/resources/application-prod.properties.example`):

| Variável | Descrição |
|---|---|
| `SPRING_PROFILES_ACTIVE` | deve ser `prod` |
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | senha do banco |
| `CORS_ALLOWED_ORIGINS` | padrões de origem permitidos, separados por vírgula (aceita `*`, ex.: `https://dozenflow.netlify.app,https://*--dozenflow.netlify.app` — o segundo padrão cobre os deploy previews do Netlify) |
| `SPRING_FLYWAY_BASELINE_ON_MIGRATE` / `SPRING_FLYWAY_BASELINE_VERSION` | `true` / `1` — necessário porque a tabela `tasks` já existia (criada pelo Hibernate) antes do Flyway ser introduzido |
| `SPRINGDOC_API_DOCS_ENABLED` / `SPRINGDOC_SWAGGER_UI_ENABLED` | `false` / `false` — desliga `/v3/api-docs` e `/swagger-ui.html` em produção (ficam ligados por padrão) |

Nunca commite um `application-prod.properties` com segredos reais — o
`.gitignore` já bloqueia esse arquivo intencionalmente.

Build da imagem Docker:

```bash
docker build -t dozenflow-be .
docker run -p 8080:8080 --env-file .env dozenflow-be
```

Em produção (Render) o serviço é implantado a partir da imagem Docker deste
repositório, com as variáveis acima configuradas no painel do Render.

### Cold start no plano free do Render

O serviço roda no plano gratuito do Render, que **desliga a instância após um
período de inatividade**. A primeira requisição depois disso "acorda" o
container, o que pode levar mais tempo do que o timeout do proxy de redirect
do Netlify — resultando num `504 Gateway Timeout` visível no frontend
(confirmado em teste manual: a 1ª tentativa de criar uma tarefa falhou com
504, a 2ª logo em seguida funcionou normalmente, já com o backend acordado).

Isso é uma limitação do plano gratuito, não um bug de código. Mitigações
possíveis, nenhuma aplicada por padrão:
- Upgrade para um plano pago do Render (sem sleep automático).
- Um serviço externo de ping/keep-alive batendo em `/actuator/health`
  periodicamente para evitar que a instância durma.
- No frontend, tratar o erro exibindo um aviso de "acordando o servidor,
  tente novamente em alguns segundos" em vez de uma falha silenciosa.

## Testes

```bash
./mvnw test      # testes unitários e de integração
./mvnw verify     # testes + relatório de cobertura (Jacoco)
```

Relatório de cobertura gerado em `target/site/jacoco/index.html`.

## Documentação adicional

- [`TUTORIAL-BACKEND.md`](TUTORIAL-BACKEND.md) — tutorial de construção da API passo a passo.
- [`CHANGELOG.md`](CHANGELOG.md) — histórico de mudanças.
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — convenções de contribuição.

## Licença

Distribuído sob a licença MIT — veja [`LICENSE`](LICENSE).
