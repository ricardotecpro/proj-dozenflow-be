CREATE TABLE board_settings (
    id BIGINT PRIMARY KEY,
    background_color_id VARCHAR(50),
    background_image BYTEA,
    background_image_content_type VARCHAR(255)
);

-- Single fixed row (id = 1), seeded with the same default the frontend
-- currently falls back to on first-ever load (see BoardBackgroundService's
-- DEFAULT_BACKGROUND_ID) so a fresh install keeps today's look.
INSERT INTO board_settings (id, background_color_id) VALUES (1, 'ocean');
