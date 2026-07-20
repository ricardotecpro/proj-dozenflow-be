# Contribuindo

## Branches

- `main` — código estável, implantável.
- `feature/<nome>` — novas funcionalidades.
- `fix/<nome>` — correções de bug.
- `hotfix/<nome>` — correções urgentes direto de produção.
- `chore/<nome>` — manutenção, dependências, infraestrutura, documentação.

Abra Pull Requests contra `main`.

## Commits

Use mensagens no formato [Conventional Commits](https://www.conventionalcommits.org/pt-br/):

```
<tipo>(<escopo opcional>): <descrição curta>
```

Tipos comuns: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `ci`.

## Rodando localmente

```bash
./mvnw spring-boot:run   # sobe com profile dev (H2 em memória)
./mvnw test               # testes unitários e de integração
./mvnw verify              # testes + cobertura (Jacoco)
```

## Antes de abrir um PR

- [ ] `./mvnw verify` passa localmente.
- [ ] Novos endpoints/comportamentos têm teste correspondente.
- [ ] `CHANGELOG.md` atualizado na seção `[Unreleased]`.
- [ ] Nenhum segredo (senha, token, URL de banco real) foi commitado.
