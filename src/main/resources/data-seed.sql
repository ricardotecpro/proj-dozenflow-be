-- Sample data for manual/visual testing in development. Only loaded when
-- the `seed` profile is active (see application-seed.properties) — never
-- runs during tests or a plain `dev` boot, so it can't interfere with
-- TaskControllerTest#getAllTasks_returnsEmptyList_whenNoTasksExist.
-- Extend this file as new tables (comments, attachments) are added in
-- later phases.
--
-- due_date uses relative offsets from CURRENT_DATE so the seed always
-- exercises overdue (past), due-soon (next 2 days) and future states,
-- whatever day this is run.
--
-- Task/label ids below rely on this running against a fresh H2 database
-- (dev,seed profile), where IDENTITY columns always start at 1: tasks get
-- ids 1-11 in the order inserted, labels are already seeded 1-8 by
-- V3__create_labels.sql (in the same order as the INSERT there: 1=green,
-- 2=yellow, 3=orange, 4=red, 5=purple, 6=blue, 7=sky, 8=lime).
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

INSERT INTO task_labels (task_id, label_id) VALUES
    (1, 4), -- Revisar proposta do cliente: vermelho
    (3, 4), -- Escrever testes E2E do checkout: vermelho
    (3, 3), -- ...e laranja (múltiplas labels no mesmo card)
    (6, 6), -- Refatorar serviço de autenticação: azul
    (8, 5), -- Integrar gateway de pagamento: roxo
    (9, 1); -- Migrar banco de dados para produção: verde

INSERT INTO checklist_items (task_id, title, done, item_order) VALUES
    (1, 'Levantar requisitos com o cliente', TRUE, 0),
    (1, 'Calcular estimativa de horas', TRUE, 1),
    (1, 'Revisar valores com o financeiro', FALSE, 2),
    (3, 'Cenário de sucesso', TRUE, 0),
    (3, 'Cenário de cartão recusado', FALSE, 1),
    (3, 'Cenário de timeout do gateway', FALSE, 2),
    (9, 'Backup do banco antigo', TRUE, 0),
    (9, 'Rodar migração', TRUE, 1),
    (9, 'Validar integridade dos dados', TRUE, 2);
