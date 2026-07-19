# Changelog

Todas as mudanças notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adere a [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added
- `README.md` com instruções reais de setup, execução e deploy.
- `LICENSE` (MIT), `CONTRIBUTING.md` e este `CHANGELOG.md`.
- `application-prod.properties.example` documentando as variáveis de ambiente
  exigidas em produção, e profile `prod` explícito (H2 console desabilitado,
  `ddl-auto` seguro).
- Security headers (HSTS, X-Frame-Options, X-Content-Type-Options,
  Referrer-Policy) no `SecurityConfig`.
- Rate limiting básico por IP nos endpoints `/api/**`.
- `spring-boot-starter-actuator` com apenas `/actuator/health` exposto.
- Testes unitários (`TaskServiceTest`, `TaskMapperTest`) e de integração
  (`TaskControllerTest`), com relatório de cobertura via Jacoco.
- Pipeline de CI no GitHub Actions (`.github/workflows/backend-ci.yml`).
- `.dockerignore` e `Dockerfile` executando como usuário não-root, com
  `HEALTHCHECK`.

### Changed
- Dependências atualizadas para as versões estáveis mais recentes compatíveis.
- `OpenApiConfig` agora referencia a licença MIT do projeto (antes citava
  Apache 2.0 incorretamente).
- `pom.xml`: preenchidos `license`, `developers` e `scm`.
- CORS (`WebConfig`) migrado de `allowedOrigins` para `allowedOriginPatterns`,
  permitindo um padrão com curinga (`https://*--dozenflow.netlify.app`) que
  cobre os deploy previews e branch previews do Netlify, além da origem de
  produção. Validado manualmente com requisições OPTIONS simulando preview,
  produção e uma origem não relacionada.

### Fixed
- `TUTORIAL-BACKEND.md` corrigido: mencionava MySQL como banco de produção,
  mas o projeto usa PostgreSQL.

### Known issues
- Cold start no plano free do Render: a primeira requisição após um período
  de inatividade pode retornar `504 Gateway Timeout` no frontend enquanto o
  container acorda (confirmado em teste manual). Ver seção "Cold start no
  plano free do Render" no `README.md` para detalhes e mitigações possíveis.

## [0.1.0] - 2025-08-27

Baseline reconstruído a partir do histórico do repositório.

### Added
- API REST de tarefas (`/api/tasks`) com CRUD completo, DTOs de
  request/response, mapeamento via `TaskMapper` e tratamento global de
  exceções (`GlobalExceptionHandler`).
- Persistência com Spring Data JPA, H2 em desenvolvimento e driver
  PostgreSQL para produção.
- Documentação da API via springdoc-openapi (Swagger UI).
- Spring Security (configuração inicial permissiva) e CORS configurável por
  variável de ambiente.
- `Dockerfile` multi-stage com Eclipse Temurin (build JDK, runtime JRE).

[Unreleased]: https://github.com/ricardotecpro/proj-dozenflow-be/compare/main...HEAD
