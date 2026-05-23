/* ==============================
   ІНДЕКСИ ДЛЯ ОПТИМІЗАЦІЇ ЗАПИТІВ
   ============================== */

CREATE INDEX IF NOT EXISTS idx_player_username ON PLAYER(USERNAME);
CREATE INDEX IF NOT EXISTS idx_player_email ON PLAYER(EMAIL);
CREATE INDEX IF NOT EXISTS idx_action_log_player_id ON ACTION_LOG(PLAYER_ID);
CREATE INDEX IF NOT EXISTS idx_action_log_created_at ON ACTION_LOG(CREATED_AT);
CREATE INDEX IF NOT EXISTS idx_player_event_history_player_id ON PLAYER_EVENT_HISTORY(PLAYER_ID);
CREATE INDEX IF NOT EXISTS idx_player_event_history_event_id ON PLAYER_EVENT_HISTORY(EVENT_ID);
CREATE INDEX IF NOT EXISTS idx_treasure_coords ON TREASURE_TEMPLATE(X, Y);

/* ==============================
   ЄДИНИЙ ГРАВЕЦЬ: АДМІНІСТРАТОР
   (пароль: admin123)
   ============================== */

INSERT INTO PLAYER (USERNAME, EMAIL, PASSWORD_HASH, X, Y, GOLD, ENERGY, CURRENT_DAY) VALUES
    ('admin', 'admin@rpg.com', 'fYY7/uqKHqRSu/xG97U13A==:EeJ/z8Lutpk1NTvF6fjEbL4t20PFtBDsrCFkSSvr2/o=', 50, 50, 999, 100, 1);

/* ==============================
   ТЕСТОВІ ДАНІ: ПОДІЇ (Потрібні для відпочинку на природі)
   ============================== */

INSERT INTO EVENT (NAME, DESCRIPTION, MIN_GOLD_PENALTY, MAX_GOLD_PENALTY) VALUES
    ('Пастка', 'Ви впали у замасковану яму! Втрачено золото.', 5, 15),
    ('Розбійники', 'На вас напали розбійники на лісовій дорозі.', 10, 30),
    ('Буря', 'Раптова буря змусила вас шукати укриття.', 3, 8),
    ('Прокляття', 'Ви активували давнє прокляття.', 15, 40),
    ('Податок', 'Місцевий лорд вимагає податок за прохід.', 5, 20);

/* ==============================
   ТЕСТОВІ ДАНІ: СКАРБИ (Потрібні для гри на мапі)
   ============================== */

INSERT INTO TREASURE_TEMPLATE (X, Y, MIN_GOLD, MAX_GOLD) VALUES
    (24, 29, 5, 10),
    (48, 41, 5, 10),
    (34, 44, 5, 10),
    (14, 54, 5, 10),
    (61, 54, 7, 15),
    (35, 60, 7, 15),
    (27, 60, 7, 15),
    (37, 57, 7, 15),
    (48, 91, 10, 20),
    (80, 26, 10, 20);

/* ==============================
   ЗБІР СКАРБІВ ДЛЯ АДМІНІСТРАТОРА
   (Всі скарби окрім останнього з ID=10 зібрані для швидкого тесту перемоги)
   ============================== */

INSERT INTO PLAYER_COLLECTED_TREASURE (PLAYER_ID, TREASURE_TEMPLATE_ID) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (1, 5),
    (1, 6),
    (1, 7),
    (1, 8),
    (1, 9);
