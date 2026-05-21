/* ==============================
   ІНДЕКСИ ДЛЯ ОПТИМІЗАЦІЇ ЗАПИТІВ
   ============================== */

CREATE INDEX idx_player_username ON PLAYER(USERNAME);
CREATE INDEX idx_player_email ON PLAYER(EMAIL);
CREATE INDEX idx_action_log_player_id ON ACTION_LOG(PLAYER_ID);
CREATE INDEX idx_action_log_created_at ON ACTION_LOG(CREATED_AT);
CREATE INDEX idx_player_event_history_player_id ON PLAYER_EVENT_HISTORY(PLAYER_ID);
CREATE INDEX idx_player_event_history_event_id ON PLAYER_EVENT_HISTORY(EVENT_ID);
CREATE INDEX idx_treasure_coords ON TREASURE_TEMPLATE(X, Y);

/* ==============================
   ТЕСТОВІ ДАНІ: ГРАВЦІ
   (пароль для всіх: password123)
   BCrypt хеш: PBKDF2WithHmacSHA256
   ============================== */

INSERT INTO PLAYER (USERNAME, EMAIL, PASSWORD_HASH, X, Y, GOLD, ENERGY, CURRENT_DAY) VALUES
    ('admin', 'admin@rpg.com', 'placeholder_hash', 5, 5, 999, 100, 1),
    ('warrior_ivan', 'ivan@mail.com', 'placeholder_hash', 10, 15, 250, 80, 5),
    ('mage_olena', 'olena@mail.com', 'placeholder_hash', 30, 20, 180, 60, 3),
    ('rogue_petro', 'petro@mail.com', 'placeholder_hash', 45, 50, 320, 45, 7),
    ('healer_anna', 'anna@mail.com', 'placeholder_hash', 22, 33, 150, 90, 2);

/* ==============================
   ТЕСТОВІ ДАНІ: ПОДІЇ
   ============================== */

INSERT INTO EVENT (NAME, DESCRIPTION, MIN_GOLD_PENALTY, MAX_GOLD_PENALTY) VALUES
    ('Пастка', 'Ви впали у замасковану яму! Втрачено золото.', 5, 15),
    ('Розбійники', 'На вас напали розбійники на лісовій дорозі.', 10, 30),
    ('Буря', 'Раптова буря змусила вас шукати укриття.', 3, 8),
    ('Прокляття', 'Ви активували древнє прокляття.', 15, 40),
    ('Податок', 'Місцевий лорд вимагає податок за прохід.', 5, 20);

/* ==============================
   ТЕСТОВІ ДАНІ: СКАРБИ
   ============================== */

INSERT INTO TREASURE_TEMPLATE (X, Y, MIN_GOLD, MAX_GOLD) VALUES
    (12, 18, 20, 50),
    (35, 42, 10, 30),
    (50, 50, 50, 100),
    (8, 90, 15, 35),
    (70, 25, 30, 60),
    (25, 60, 5, 15),
    (88, 12, 40, 80);

-- Заселяємо зібраний скарб для гравця warrior_ivan (ID=2)
INSERT INTO PLAYER_COLLECTED_TREASURE (PLAYER_ID, TREASURE_TEMPLATE_ID) VALUES (2, 4);

/* ==============================
   ТЕСТОВІ ДАНІ: ЛОГИ ДІЙ
   ============================== */

INSERT INTO ACTION_LOG (PLAYER_ID, ACTION_TYPE, FROM_X, FROM_Y, TO_X, TO_Y, IS_VALID, CREATED_AT) VALUES
    (1, 'MOVE', 4, 4, 5, 5, TRUE, CURRENT_TIMESTAMP),
    (2, 'MOVE', 9, 14, 10, 15, TRUE, CURRENT_TIMESTAMP),
    (2, 'COLLECT', 10, 15, 10, 15, TRUE, CURRENT_TIMESTAMP),
    (3, 'MOVE', 29, 19, 30, 20, TRUE, CURRENT_TIMESTAMP),
    (4, 'MOVE', 44, 49, 45, 50, TRUE, CURRENT_TIMESTAMP),
    (4, 'MOVE', 0, 0, 45, 50, FALSE, CURRENT_TIMESTAMP);

/* ==============================
   ТЕСТОВІ ДАНІ: ІСТОРІЯ ПОДІЙ ГРАВЦІВ (N:M)
   ============================== */

INSERT INTO PLAYER_EVENT_HISTORY (PLAYER_ID, EVENT_ID, OCCURRED_DAY) VALUES
    (2, 1, 2),
    (2, 3, 4),
    (3, 2, 1),
    (4, 4, 3),
    (4, 1, 5),
    (5, 5, 1);
