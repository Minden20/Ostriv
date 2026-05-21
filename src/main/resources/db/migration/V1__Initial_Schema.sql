/* ТАБЛИЦЯ: PLAYER
   Нормальна форма: 3NF
   Класифікація: Майстер-дані (основна сутність гравця)
*/
CREATE TABLE PLAYER (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    USERNAME VARCHAR(255) UNIQUE NOT NULL,
    EMAIL VARCHAR(255) UNIQUE NOT NULL,
    PASSWORD_HASH VARCHAR(255) NOT NULL,
    X INT,
    Y INT,
    GOLD INT,
    ENERGY INT,
    CURRENT_DAY INT
);

/* ТАБЛИЦЯ: MAP_TILE
   Класифікація: Довідник / Глобальний шаблон
   (Твоя статична мапа. Сюди дані заливаються ОДИН раз для всієї гри)
*/
CREATE TABLE MAP_TILE (
    X INT,
    Y INT,
    TERRAIN_TYPE VARCHAR(255),
    PRIMARY KEY (X, Y)
);

/* ТАБЛИЦЯ: PLAYER_MAP_MODIFICATION
   Класифікація: Транзакційна таблиця (Персональні зміни мапи)
   (Рядок створюється ТІЛЬКИ якщо конкретний гравець змінив тайл: зрубав ліс, побудував щось)
*/
CREATE TABLE PLAYER_MAP_MODIFICATION (
    PLAYER_ID INT,
    X INT,
    Y INT,
    NEW_TERRAIN_TYPE VARCHAR(255),
    PRIMARY KEY (PLAYER_ID, X, Y),
    FOREIGN KEY (PLAYER_ID) REFERENCES PLAYER(ID) ON DELETE CASCADE
);

/* ТАБЛИЦЯ: TREASURE_TEMPLATE
   Класифікація: Довідник / Глобальний шаблон
   (Точки на мапі, де в теорії з самого початку спавняться скарби для всіх)
*/
CREATE TABLE TREASURE_TEMPLATE (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    X INT,
    Y INT,
    MIN_GOLD INT,
    MAX_GOLD INT
);

/* ТАБЛИЦЯ: PLAYER_COLLECTED_TREASURE
   Класифікація: Транзакційна (Зв'язок Багато-до-Багатьох)
   (Якщо тут є запис — значить цей конкретний гравець цей скарб уже забрав)
*/
CREATE TABLE PLAYER_COLLECTED_TREASURE (
    PLAYER_ID INT,
    TREASURE_TEMPLATE_ID INT,
    PRIMARY KEY (PLAYER_ID, TREASURE_TEMPLATE_ID),
    FOREIGN KEY (PLAYER_ID) REFERENCES PLAYER(ID) ON DELETE CASCADE,
    FOREIGN KEY (TREASURE_TEMPLATE_ID) REFERENCES TREASURE_TEMPLATE(ID) ON DELETE CASCADE
);

/* ТАБЛИЦЯ: ACTION_LOG
   Класифікація: Транзакційна таблиця (лог дій)
*/
CREATE TABLE ACTION_LOG (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    PLAYER_ID INT,
    ACTION_TYPE VARCHAR(255),
    FROM_X INT,
    FROM_Y INT,
    TO_X INT,
    TO_Y INT,
    IS_VALID BOOLEAN,
    CREATED_AT DATETIME,
    FOREIGN KEY (PLAYER_ID) REFERENCES PLAYER(ID) ON DELETE CASCADE
);

/* ТАБЛИЦЯ: EVENT
   Класифікація: Довідник (список можливих подій)
*/
CREATE TABLE EVENT (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(255),
    DESCRIPTION TEXT,
    MIN_GOLD_PENALTY INT,
    MAX_GOLD_PENALTY INT
);

/* ТАБЛИЦЯ: PLAYER_EVENT_HISTORY
   Класифікація: Транзакційна таблиця (особиста історія подій)
*/
CREATE TABLE PLAYER_EVENT_HISTORY (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    PLAYER_ID INT,
    EVENT_ID INT,
    OCCURRED_DAY INT,
    FOREIGN KEY (PLAYER_ID) REFERENCES PLAYER(ID) ON DELETE CASCADE,
    FOREIGN KEY (EVENT_ID) REFERENCES EVENT(ID) ON DELETE CASCADE
);