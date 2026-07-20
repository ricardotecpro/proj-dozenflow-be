-- Sample data for manual/visual testing in development. Only loaded when
-- the `seed` profile is active (see application-seed.properties) — never
-- runs during tests or a plain `dev` boot, so it can't interfere with
-- TaskControllerTest#getAllTasks_returnsEmptyList_whenNoTasksExist.
-- The card-features migrations end at V7 (cover color); V8 adds the
-- task_lists table and archiving — nothing left to extend here for now.
--
-- due_date uses relative offsets from CURRENT_DATE so the seed always
-- exercises overdue (past), due-soon (next 2 days) and future states,
-- whatever day this is run.
--
-- Task/label/list ids below rely on this running against a fresh H2
-- database (dev,seed profile), where IDENTITY columns always start at 1:
-- tasks get ids 1-11 in the order inserted, labels are already seeded 1-8
-- by V3__create_labels.sql (in the same order as the INSERT there:
-- 1=green, 2=yellow, 3=orange, 4=red, 5=purple, 6=blue, 7=sky, 8=lime),
-- and lists are seeded 1-3 by V8__add_task_lists_and_archiving.sql
-- (1=A Fazer, 2=Em Andamento, 3=Concluída).
INSERT INTO tasks (title, description, list_id, task_order, due_date) VALUES
    ('Revisar proposta do cliente', 'Conferir escopo e valores antes de enviar', 1, 0, DATEADD('DAY', -2, CURRENT_DATE)),
    ('Configurar ambiente de staging', NULL, 1, 1, NULL),
    ('Escrever testes E2E do checkout', 'Cobrir os fluxos de sucesso e falha de pagamento', 1, 2, DATEADD('DAY', 1, CURRENT_DATE)),
    ('Atualizar dependências do projeto', NULL, 1, 3, DATEADD('DAY', 10, CURRENT_DATE)),
    ('Planejar sprint da próxima semana', 'Alinhar prioridades com o time de produto', 1, 4, NULL),
    ('Refatorar serviço de autenticação', 'Extrair validação de token para um componente separado', 2, 0, DATEADD('DAY', 2, CURRENT_DATE)),
    ('Corrigir bug no upload de imagens', NULL, 2, 1, DATEADD('DAY', -1, CURRENT_DATE)),
    ('Integrar gateway de pagamento', 'Ambiente sandbox já configurado, falta o fluxo de estorno', 2, 2, NULL),
    ('Migrar banco de dados para produção', NULL, 3, 0, DATEADD('DAY', -5, CURRENT_DATE)),
    ('Configurar CI/CD', 'Pipeline com lint, testes e build automatizados', 3, 1, NULL),
    ('Deploy da versão 1.0', NULL, 3, 2, DATEADD('DAY', -3, CURRENT_DATE));

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

INSERT INTO comments (task_id, body, created_at) VALUES
    (1, 'Cliente pediu pra priorizar isso — combinei entrega até sexta.', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
    (3, 'Cenário de sucesso já está passando localmente.', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
    (3, 'Faltam os cenários de erro ainda.', DATEADD('HOUR', -1, CURRENT_TIMESTAMP));

-- One small text attachment for visual QA of the attachment count badge
-- and download flow. Bytes are the UTF-8 hex encoding of:
-- "Notas da reunião com o cliente sobre a proposta."
INSERT INTO attachments (task_id, file_name, content_type, size_bytes, data, created_at) VALUES
    (1, 'notas-reuniao.txt', 'text/plain', 49,
     X'4e6f746173206461207265756e69c3a36f20636f6d206f20636c69656e746520736f62726520612070726f706f7374612e',
     DATEADD('DAY', -1, CURRENT_TIMESTAMP));
