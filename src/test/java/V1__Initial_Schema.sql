/* ТАБЛИЦЯ: PLAYER
   Нормальна форма: 3NF (всі дані атомарні, залежать від ID, немає транзитивних залежностей)
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
   Нормальна форма: 2NF (залежність від складеного ключа X, Y)
   Класифікація: Довідник/Словник (статична карта світу)
*/
CREATE TABLE MAP_TILE (
    X INT,
    Y INT,
    TERRAIN_TYPE VARCHAR(255),
    PRIMARY KEY (X, Y)
);

/* ТАБЛИЦЯ: TREASURE
   Нормальна форма: 3NF
   Класифікація: Майстер-дані (ігрові об'єкти)
*/
CREATE TABLE TREASURE (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    X INT,
    Y INT,
    MIN_GOLD INT,
    MAX_GOLD INT,
    IS_COLLECTED BOOLEAN
);

/* ТАБЛИЦЯ: ACTION_LOG
   Нормальна форма: 3NF
   Класифікація: Транзакційна таблиця (лог дій для античиту)
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
    FOREIGN KEY (PLAYER_ID) REFERENCES PLAYER(ID)
);

/* ТАБЛИЦЯ: EVENT
   Нормальна форма: 3NF
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
   Нормальна форма: 3NF
   Класифікація: Транзакційна таблиця (зв'язок Багато-до-Багатьох)
*/
CREATE TABLE PLAYER_EVENT_HISTORY (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    PLAYER_ID INT,
    EVENT_ID INT,
    OCCURRED_DAY INT,
    FOREIGN KEY (PLAYER_ID) REFERENCES PLAYER(ID),
    FOREIGN KEY (EVENT_ID) REFERENCES EVENT(ID)
);