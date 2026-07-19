-- Sample data for manual/visual testing in development. Only loaded when
-- the `seed` profile is active (see application-seed.properties) — never
-- runs during tests or a plain `dev` boot, so it can't interfere with
-- TaskControllerTest#getAllTasks_returnsEmptyList_whenNoTasksExist.
-- Extend this file as new tables (labels, checklist_items, comments,
-- attachments) are added in later phases.
INSERT INTO tasks (title, description, status, task_order) VALUES
    ('Revisar proposta do cliente', 'Conferir escopo e valores antes de enviar', 'A_FAZER', 0),
    ('Configurar ambiente de staging', NULL, 'A_FAZER', 1),
    ('Escrever testes E2E do checkout', 'Cobrir os fluxos de sucesso e falha de pagamento', 'A_FAZER', 2),
    ('Atualizar dependências do projeto', NULL, 'A_FAZER', 3),
    ('Planejar sprint da próxima semana', 'Alinhar prioridades com o time de produto', 'A_FAZER', 4),
    ('Refatorar serviço de autenticação', 'Extrair validação de token para um componente separado', 'EM_ANDAMENTO', 0),
    ('Corrigir bug no upload de imagens', NULL, 'EM_ANDAMENTO', 1),
    ('Integrar gateway de pagamento', 'Ambiente sandbox já configurado, falta o fluxo de estorno', 'EM_ANDAMENTO', 2),
    ('Migrar banco de dados para produção', NULL, 'CONCLUIDA', 0),
    ('Configurar CI/CD', 'Pipeline com lint, testes e build automatizados', 'CONCLUIDA', 1),
    ('Deploy da versão 1.0', NULL, 'CONCLUIDA', 2);
