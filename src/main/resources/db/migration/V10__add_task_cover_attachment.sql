ALTER TABLE tasks ADD COLUMN cover_attachment_id BIGINT REFERENCES attachments(id) ON DELETE SET NULL;
