# Changelog

Todas as mudanças notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adere a [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added
- Listas dinâmicas do board (`TaskList`, substituindo o enum fixo
  `TaskStatus`): CRUD completo (`GET/POST/PUT /api/lists`), reordenação por
  posição, e arquivamento (`POST /api/lists/{id}/archive`, cascateando o
  arquivamento pros cards da lista; `POST .../restore`) em vez de exclusão
  definitiva — a exclusão real (`DELETE`) fica reservada pro painel de
  itens arquivados do frontend. Guarda contra arquivar/excluir a última
  lista ativa do board (`LastActiveListException` → 400).
  `V8__add_task_lists_and_archiving.sql` cria `task_lists` e as colunas
  `list_id`/`archived` em `tasks` (substituindo a antiga coluna `status`).
- Arquivamento de tarefas (`POST /api/tasks/{id}/archive`, `.../restore`,
  `GET /api/tasks/archived`), mesmo espírito de soft-delete das listas:
  restaurar uma tarefa cuja lista ainda está arquivada restaura a lista
  automaticamente junto, pra não deixar cartão "órfão" sem coluna visível
  no board.
- Anexos nas tarefas: upload/listagem/download/exclusão de arquivo por
  tarefa via `/api/tasks/{taskId}/attachments` (`V6__create_attachments.sql`).
  Decisão de arquitetura (confirmada com o usuário): arquivo guardado como
  blob no próprio banco (`bytea`, coluna `data`), não em disco nem em
  storage externo — evita o problema do disco efêmero do Render (some a
  cada redeploy) sem precisar de conta/credencial de serviço novo. Limite
  de 5MB por arquivo e allowlist de `content-type` (imagens comuns, PDF,
  texto, Word/Excel). A listagem usa uma projeção do Spring Data que
  exclui a coluna `data` — nunca carrega os bytes só pra mostrar nomes de
  arquivo. `TaskResponseDTO` ganha `attachmentCount`, mas ao contrário de
  labels/checklist/comentários (que usam uma coleção JPA `EAGER`+
  `SUBSELECT`), aqui uso `@Formula` (subquery `COUNT` correlacionada) —
  uma coleção seria carregar todo o blob de cada tarefa só pra contar.
- Comentários nas tarefas: adicionar/listar/excluir comentários por tarefa
  via `/api/tasks/{taskId}/comments` (`V5__create_comments.sql`). Sem
  edição (só criar e excluir) e sem dono/autor — consistente com o modelo
  sem autenticação do projeto, qualquer pessoa pode excluir qualquer
  comentário. `TaskResponseDTO` ganha `commentCount`, mesma estratégia de
  fetch `EAGER`+`SUBSELECT` das labels/checklist.
- Checklist nas tarefas: itens de checklist (título + concluído/pendente)
  por tarefa via `/api/tasks/{taskId}/checklist-items`
  (`GET/POST` na coleção, `PUT/DELETE` por item — `V4__create_checklist_items.sql`).
  `TaskResponseDTO` ganha `checklistTotal`/`checklistDone` (contagem, não a
  lista completa — a lista cheia só é buscada quando o diálogo da tarefa
  abre, evitando overfetch na visão do board). Mesma estratégia de fetch
  `EAGER`+`SUBSELECT` das labels, pelo mesmo motivo (`open-in-view=false`).
- Labels coloridas nas tarefas: catálogo de labels do board
  (`GET/POST /api/labels`, `PUT/DELETE /api/labels/{id}`) com a paleta
  clássica do Trello pré-populada (`V3__create_labels.sql`), e associação
  many-to-many com tarefas via `POST/DELETE /api/tasks/{id}/labels/{labelId}`.
  `Task.labels` usa fetch `EAGER` + `FetchMode.SUBSELECT` (não o `LAZY`
  padrão do JPA) porque `spring.jpa.open-in-view=false` significa que o
  mapper lê essa coleção depois que a transação do service já fechou —
  `EAGER` evita `LazyInitializationException` e `SUBSELECT` evita N+1
  (1 query extra para todas as tarefas, não uma por tarefa). Excluir uma
  label remove a associação de todas as tarefas via `ON DELETE CASCADE`.
- Campo opcional `dueDate` (data de vencimento) em `Task`, exposto em
  `POST/PUT /api/tasks` e nas respostas da API (`V2__add_due_date.sql`).
  Primeiro de uma série de recursos novos de card estilo Trello (labels,
  checklist, comentários e anexos vêm nas próximas migrações).
- Migrações de schema via Flyway (`src/main/resources/db/migration`),
  substituindo `ddl-auto` autogerado pelo Hibernate como fonte de verdade
  do banco (agora só valida). `V1__baseline.sql` reproduz o schema atual
  da tabela `tasks`; ambiente de produção precisa das variáveis
  `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` e `SPRING_FLYWAY_BASELINE_VERSION=1`
  no primeiro deploy após essa mudança, já que a tabela já existe lá (ver
  README). Preparação para os novos recursos de card (labels, checklist,
  prazo, comentários e anexos) que virão nas próximas migrações.
- Profile opcional `seed` (`SPRING_PROFILES_ACTIVE=dev,seed`) que popula o
  H2 de desenvolvimento com tarefas de exemplo (`data-seed.sql`), só para
  testes manuais/visuais — nunca ativo em testes automatizados ou produção.
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

### Security
- Swagger UI e `/v3/api-docs` desligados em produção
  (`springdoc.api-docs.enabled`/`springdoc.swagger-ui.enabled=false`) —
  antes ficavam expostos publicamente por padrão, revelando o schema
  completo da API em URLs conhecidas.
- Removida a autenticação in-memory padrão do Spring Security
  (`UserDetailsServiceAutoConfiguration` excluída na classe principal):
  como todo endpoint é `permitAll`, o usuário/senha aleatórios gerados a
  cada boot ("Using generated security password") não protegiam nada, só
  poluíam o log.
- Branch padrão do repositório (renomeado de `hotfix/add-h2-and-task-table`
  para `main`) protegido no GitHub: exige o check `build-and-test` do CI
  passando antes de qualquer merge.
- Senha do usuário do banco no Neon rotacionada após ter sido exposta
  acidentalmente durante uma investigação de conectividade.

### Fixed
- **Bug crítico de persistência em produção**: o serviço no Render nunca
  teve `SPRING_PROFILES_ACTIVE=prod` configurado, então rodava sempre no
  profile `dev` (padrão de `application.properties`) com banco H2 em
  memória — todo dado de "produção" era apagado a cada sleep/restart da
  instância free, sem que ninguém percebesse. As variáveis existentes
  (`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`) também tinham nome errado e nunca
  eram lidas pelo Spring (que espera `SPRING_DATASOURCE_*`). Corrigido
  configurando as variáveis certas no Render mais `SPRING_PROFILES_ACTIVE=
  prod`; `SPRING_FLYWAY_BASELINE_ON_MIGRATE`/`_VERSION=1` reconciliam o
  Flyway com a tabela `tasks` legada que já existia no banco Postgres real
  (Neon), nunca antes conectado de fato.
- `TaskRepository.archiveAllByListId` (bulk update `@Modifying` usado pelo
  cascade de arquivamento de listas) estava sem `flushAutomatically = true`
  — o `clearAutomatically = true` sozinho descartava silenciosamente a
  mudança pendente `TaskList.archived = true` antes dela ser gravada no
  banco, fazendo a lista "voltar" a aparecer como ativa.
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
