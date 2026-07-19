-- Sample data for manual/visual testing in development. Only loaded when
-- the `seed` profile is active (see application-seed.properties) — never
-- runs during tests or a plain `dev` boot, so it can't interfere with
-- TaskControllerTest#getAllTasks_returnsEmptyList_whenNoTasksExist.
-- Extend this file as new tables (labels, checklist_items, comments,
-- attachments) are added in later phases.
--
-- due_date uses relative offsets from CURRENT_DATE so the seed always
-- exercises overdue (past), due-soon (next 2 days) and future states,
-- whatever day this is run.
INSERT INTO tasks (title, description, status, task_order, due_date) VALUES
    ('Revisar proposta do cliente', 'Conferir escopo e valores antes de enviar', 'A_FAZER', 0, DATEADD('DAY', -2, CURRENT_DATE)),
    ('Configurar ambiente de staging', NULL, 'A_FAZER', 1, NULL),
    ('Escrever testes E2E do checkout', 'Cobrir os fluxos de sucesso e falha de pagamento', 'A_FAZER', 2, DATEADD('DAY', 1, CURRENT_DATE)),
    ('Atualizar dependências do projeto', NULL, 'A_FAZER', 3, DATEADD('DAY', 10, CURRENT_DATE)),
    ('Planejar sprint da próxima semana', 'Alinhar prioridades com o time de produto', 'A_FAZER', 4, NULL),
    ('Refatorar serviço de autenticação', 'Extrair validação de token para um componente separado', 'EM_ANDAMENTO', 0, DATEADD('DAY', 2, CURRENT_DATE)),
    ('Corrigir bug no upload de imagens', NULL, 'EM_ANDAMENTO', 1, DATEADD('DAY', -1, CURRENT_DATE)),
    ('Integrar gateway de pagamento', 'Ambiente sandbox já configurado, falta o fluxo de estorno', 'EM_ANDAMENTO', 2, NULL),
    ('Migrar banco de dados para produção', NULL, 'CONCLUIDA', 0, DATEADD('DAY', -5, CURRENT_DATE)),
    ('Configurar CI/CD', 'Pipeline com lint, testes e build automatizados', 'CONCLUIDA', 1, NULL),
    ('Deploy da versão 1.0', NULL, 'CONCLUIDA', 2, DATEADD('DAY', -3, CURRENT_DATE));
