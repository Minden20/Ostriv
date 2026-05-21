```Java
package com.minden;

import com.minden.entity.ConnectionPool;
import com.minden.entity.Player;
import com.minden.repository.PlayerRepository;
import com.minden.repository.PlayerRepositoryImpl;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== СТАРТ КОМПЛЕКСНОГО ТЕСТУВАННЯ INFRASTRUCTURE LAYER ===");

        try {
            ConnectionPool connectionPool = new ConnectionPool(5);
            PlayerRepository playerRepository = new PlayerRepositoryImpl(connectionPool);

            // ==========================================
            // ГРУПА 1: НЕЗАЛЕЖНІ ТАБЛИЦІ
            // ==========================================

            // 1. Тест PLAYER
            System.out.println("\n[1/5] Тестуємо таблицю PLAYER...");
            Player player = Player.builder()
                    .username("Aragon_" + System.currentTimeMillis())
                    .email("aragon" + System.currentTimeMillis() + "@mail.com")
                    .passwordHash("hash123")
                    .x(0).y(0).gold(100).energy(50).currentDay(1).build();
            playerRepository.save(player);
            System.out.println("✅ PLAYER збережено! ID: " + player.getId());

            // 2. Тест TREASURE
            System.out.println("\n[2/5] Тестуємо таблицю TREASURE...");
            int treasureId = 0;
            try (var conn = connectionPool.getConnection();
                 var stmt = conn.prepareStatement("INSERT INTO TREASURE (X, Y, MIN_GOLD, MAX_GOLD, IS_COLLECTED) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, 5); stmt.setInt(2, 5); stmt.setInt(3, 10); stmt.setInt(4, 50); stmt.setBoolean(5, false);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) treasureId = rs.getInt(1);
                System.out.println("✅ TREASURE збережено! ID: " + treasureId);
            }

            // 3. Тест EVENT
            System.out.println("\n[3/5] Тестуємо таблицю EVENT...");
            int eventId = 0;
            try (var conn = connectionPool.getConnection();
                 var stmt = conn.prepareStatement("INSERT INTO EVENT (NAME, DESCRIPTION, MIN_GOLD_PENALTY, MAX_GOLD_PENALTY) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, "Пастка"); stmt.setString(2, "Ви впали у яму!"); stmt.setInt(3, 5); stmt.setInt(4, 15);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) eventId = rs.getInt(1);
                System.out.println("✅ EVENT збережено! ID: " + eventId);
            }

            // ==========================================
            // ГРУПА 2: ЗАЛЕЖНІ ТАБЛИЦІ (потребують ID з Групи 1)
            // ==========================================

            // 4. Тест ACTION_LOG (потребує Player ID)
            System.out.println("\n[4/5] Тестуємо таблицю ACTION_LOG (Foreign Key: Player ID)...");
            try (var conn = connectionPool.getConnection();
                 var stmt = conn.prepareStatement("INSERT INTO ACTION_LOG (PLAYER_ID, ACTION_TYPE, FROM_X, FROM_Y, TO_X, TO_Y, IS_VALID, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setInt(1, player.getId());
                stmt.setString(2, "MOVE");
                stmt.setInt(3, 0); stmt.setInt(4, 0); stmt.setInt(5, 1); stmt.setInt(6, 1);
                stmt.setBoolean(7, true);
                stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                stmt.executeUpdate();
                System.out.println("✅ ACTION_LOG успішно збережено та прив'язано до Гравця " + player.getId());
            }

            // 5. Тест PLAYER_EVENT_HISTORY (потребує Player ID та Event ID)
            System.out.println("\n[5/5] Тестуємо таблицю PLAYER_EVENT_HISTORY (Багато-до-Багатьох)...");
            try (var conn = connectionPool.getConnection();
                 var stmt = conn.prepareStatement("INSERT INTO PLAYER_EVENT_HISTORY (PLAYER_ID, EVENT_ID, OCCURRED_DAY) VALUES (?, ?, ?)")) {
                stmt.setInt(1, player.getId());
                stmt.setInt(2, eventId);
                stmt.setInt(3, 1);
                stmt.executeUpdate();
                System.out.println("✅ PLAYER_EVENT_HISTORY успішно збережено! Гравець " + player.getId() + " зустрів Подію " + eventId);
            }

            System.out.println("\n🎉 УСІ 5 ТАБЛИЦЬ УСПІШНО ПРОТЕСТОВАНО! ІНФРАСТРУКТУРА ПРАЦЮЄ ІДЕАЛЬНО.");

        } catch (Exception e) {
            System.out.println("❌ Сталася помилка під час тестування:");
            e.printStackTrace();
        }
    }
}
```
[1/5] Тестуємо таблицю PLAYER...
✅ PLAYER збережено! ID: 1

[2/5] Тестуємо таблицю TREASURE...
✅ TREASURE збережено! ID: 1

[3/5] Тестуємо таблицю EVENT...
✅ EVENT збережено! ID: 1

[4/5] Тестуємо таблицю ACTION_LOG (Foreign Key: Player ID)...
✅ ACTION_LOG успішно збережено та прив'язано до Гравця 1

[5/5] Тестуємо таблицю PLAYER_EVENT_HISTORY (Багато-до-Багатьох)...
✅ PLAYER_EVENT_HISTORY успішно збережено! Гравець 1 зустрів Подію 1

🎉 УСІ 5 ТАБЛИЦЬ УСПІШНО ПРОТЕСТОВАНО! ІНФРАСТРУКТУРА ПРАЦЮЄ ІДЕАЛЬНО.