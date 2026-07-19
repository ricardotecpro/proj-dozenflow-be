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
| `CORS_ALLOWED_ORIGINS` | origens permitidas (ex.: `https://dozenflow.netlify.app`) |

Nunca commite um `application-prod.properties` com segredos reais — o
`.gitignore` já bloqueia esse arquivo intencionalmente.

Build da imagem Docker:

```bash
docker build -t dozenflow-be .
docker run -p 8080:8080 --env-file .env dozenflow-be
```

Em produção (Render) o serviço é implantado a partir da imagem Docker deste
repositório, com as variáveis acima configuradas no painel do Render.

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
