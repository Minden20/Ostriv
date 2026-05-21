

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InfrastructureIntegrationTest {

    private static JdbcDataSource testDataSource;

    // Цей метод запускається ОДИН РАЗ перед усіма тестами
    @BeforeAll
    static void setupTestDatabase() {
        // 1. Створюємо підключення до тимчасової БД у пам'яті (mem)
        testDataSource = new JdbcDataSource();
        testDataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        testDataSource.setUser("sa");
        testDataSource.setPassword("");

        // 2. Запускаємо Flyway, щоб він створив усі таблиці з ваших SQL-скриптів
        Flyway flyway = Flyway.configure()
                .dataSource(testDataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }

    @Test
    void shouldSuccessfullySavePlayerAndAutoGenerateId() {
        try (Connection conn = testDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
             
            // Act: Вставляємо гравця
            stmt.executeUpdate("INSERT INTO PLAYER (USERNAME, EMAIL, PASSWORD_HASH, X, Y, GOLD, ENERGY, CURRENT_DAY) " +
                    "VALUES ('TestHero', 'test@hero.com', 'hash', 10, 10, 100, 50, 1)");

            // Assert: Перевіряємо, чи зберігся він і чи згенерувався ID
            ResultSet rs = stmt.executeQuery("SELECT * FROM PLAYER WHERE USERNAME = 'TestHero'");
            assertTrue(rs.next(), "Гравець має бути знайдений у базі");
            assertNotNull(rs.getObject("ID"), "ID має генеруватися автоматично (AUTO_INCREMENT)");
            assertEquals("test@hero.com", rs.getString("EMAIL"), "Email має збігатися");
            
        } catch (Exception e) {
            fail("Тест впав з помилкою: " + e.getMessage());
        }
    }

    @Test
    void shouldFailWhenSavingPlayerWithDuplicateEmail() {
        Exception exception = null;
        try (Connection conn = testDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
             
            // Вставляємо першого гравця
            stmt.executeUpdate("INSERT INTO PLAYER (USERNAME, EMAIL, PASSWORD_HASH, X, Y, GOLD, ENERGY, CURRENT_DAY) " +
                    "VALUES ('Hero1', 'duplicate@mail.com', 'hash', 0, 0, 0, 0, 1)");

            // Act: Спроба вставити ДРУГОГО гравця з ТИМ САМИМ email
            stmt.executeUpdate("INSERT INTO PLAYER (USERNAME, EMAIL, PASSWORD_HASH, X, Y, GOLD, ENERGY, CURRENT_DAY) " +
                    "VALUES ('Hero2', 'duplicate@mail.com', 'hash', 0, 0, 0, 0, 1)");

        } catch (Exception e) {
            exception = e;
        }

        // Assert: Має викинутися помилка про порушення унікальності (UNIQUE constraint)
        assertNotNull(exception, "Має бути помилка бази даних");
        assertTrue(exception.getMessage().contains("Unique index or primary key violation") || 
                   exception.getMessage().contains("UNIQUE constraint"), 
                   "Помилка має бути пов'язана з дублікатом Email");
    }

    @Test
    void shouldSuccessfullyLinkActionLogToPlayer() {
        try (Connection conn = testDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
             
            // Arrange: Створюємо гравця
            stmt.executeUpdate("INSERT INTO PLAYER (USERNAME, EMAIL, PASSWORD_HASH, X, Y, GOLD, ENERGY, CURRENT_DAY) " +
                    "VALUES ('LogHero', 'log@hero.com', 'hash', 0, 0, 0, 0, 1)");
            
            ResultSet rs = stmt.executeQuery("SELECT ID FROM PLAYER WHERE USERNAME = 'LogHero'");
            rs.next();
            int playerId = rs.getInt("ID");

            // Act: Записуємо лог переміщення для цього гравця
            var pstmt = conn.prepareStatement("INSERT INTO ACTION_LOG (PLAYER_ID, ACTION_TYPE, FROM_X, FROM_Y, TO_X, TO_Y, IS_VALID, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, playerId);
            pstmt.setString(2, "MOVE");
            pstmt.setInt(3, 0); pstmt.setInt(4, 0); pstmt.setInt(5, 1); pstmt.setInt(6, 1);
            pstmt.setBoolean(7, true);
            pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();

            // Assert: Перевіряємо, чи лог існує
            ResultSet logRs = stmt.executeQuery("SELECT * FROM ACTION_LOG WHERE PLAYER_ID = " + playerId);
            assertTrue(logRs.next(), "Лог подій має бути успішно збережений");
            assertEquals("MOVE", logRs.getString("ACTION_TYPE"));

        } catch (Exception e) {
            fail("Тест впав з помилкою: " + e.getMessage());
        }
    }

    @Test
    void shouldSuccessfullyLinkPlayerToCollectedTreasure() {
        try (Connection conn = testDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
             
            // Arrange: Створюємо гравця
            stmt.executeUpdate("INSERT INTO PLAYER (USERNAME, EMAIL, PASSWORD_HASH, X, Y, GOLD, ENERGY, CURRENT_DAY) " +
                    "VALUES ('TreasureHero', 'treasure@hero.com', 'hash', 0, 0, 0, 0, 1)");
            ResultSet rs = stmt.executeQuery("SELECT ID FROM PLAYER WHERE USERNAME = 'TreasureHero'");
            rs.next();
            int playerId = rs.getInt("ID");

            // Arrange: Створюємо шаблон скарбу
            stmt.executeUpdate("INSERT INTO TREASURE_TEMPLATE (X, Y, MIN_GOLD, MAX_GOLD) " +
                    "VALUES (15, 15, 10, 50)", Statement.RETURN_GENERATED_KEYS);
            ResultSet rsTreasure = stmt.getGeneratedKeys();
            rsTreasure.next();
            int treasureId = rsTreasure.getInt(1);

            // Act: Записуємо збір скарбу гравцем
            stmt.executeUpdate(String.format("INSERT INTO PLAYER_COLLECTED_TREASURE (PLAYER_ID, TREASURE_TEMPLATE_ID) VALUES (%d, %d)", playerId, treasureId));

            // Assert: Перевіряємо, чи існує зв'язок
            ResultSet collectedRs = stmt.executeQuery(String.format("SELECT * FROM PLAYER_COLLECTED_TREASURE WHERE PLAYER_ID = %d AND TREASURE_TEMPLATE_ID = %d", playerId, treasureId));
            assertTrue(collectedRs.next(), "Зв'язок збору скарбу має бути успішно збережений");

        } catch (Exception e) {
            fail("Тест збору скарбів впав з помилкою: " + e.getMessage());
        }
    }
}