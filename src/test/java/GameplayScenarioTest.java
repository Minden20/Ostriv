
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.minden.dto.TreasureDto;
import com.minden.entity.ActionLog;
import com.minden.entity.ConnectionPool;
import com.minden.entity.Player;
import com.minden.repository.ActionLogRepository;
import com.minden.repository.ActionLogRepositoryImpl;
import com.minden.repository.MapTileRepository;
import com.minden.repository.MapTileRepositoryImpl;
import com.minden.repository.PlayerRepository;
import com.minden.repository.PlayerRepositoryImpl;
import com.minden.repository.TreasureRepository;
import com.minden.repository.TreasureRepositoryImpl;
import com.minden.service.TreasureService;
import com.minden.service.TreasureServiceImpl;

public class GameplayScenarioTest {

    private static JdbcDataSource testDataSource;
    private static ConnectionPool testConnectionPool;

    private PlayerRepository playerRepository;
    private TreasureRepository treasureRepository;
    private MapTileRepository mapTileRepository;
    private ActionLogRepository actionLogRepository;

    private TreasureService treasureService;

    private static class TestConnectionPool extends ConnectionPool {

        private final JdbcDataSource ds;

        public TestConnectionPool(JdbcDataSource ds) throws SQLException {
            super(1);
            this.ds = ds;
        }

        @Override
        public Connection getConnection() {
            try {
                return ds.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Не вдалося отримати з'єднання з тестовою БД: " + e.getMessage());
            }
        }

        @Override
        public void releaseConnection(Connection connection) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @BeforeAll
    static void setupTestDatabase() throws SQLException {
        // 1. Створюємо підключення до тимчасової БД у пам'яті (mem)
        testDataSource = new JdbcDataSource();
        testDataSource.setURL("jdbc:h2:mem:gameplayscenariodb;DB_CLOSE_DELAY=-1");
        testDataSource.setUser("sa");
        testDataSource.setPassword("");

        // 2. Запускаємо Flyway для міграцій на тестовій БД
        Flyway flyway = Flyway.configure()
                .dataSource(testDataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        // 3. Ініціалізуємо тестовий пул з'єднань
        testConnectionPool = new TestConnectionPool(testDataSource);
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Очищаємо таблиці перед кожним тестом для ізоляції
        try (Connection conn = testDataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM PLAYER_COLLECTED_TREASURE");
            stmt.executeUpdate("DELETE FROM ACTION_LOG");
            stmt.executeUpdate("DELETE FROM PLAYER_MAP_MODIFICATION");
            stmt.executeUpdate("DELETE FROM PLAYER_EVENT_HISTORY");
            stmt.executeUpdate("DELETE FROM PLAYER WHERE USERNAME <> 'admin'");
            stmt.executeUpdate("DELETE FROM TREASURE_TEMPLATE");
        }

        // Створюємо екземпляри репозиторіїв та сервісів з тестовим пулом
        playerRepository = new PlayerRepositoryImpl(testConnectionPool);
        treasureRepository = new TreasureRepositoryImpl(testConnectionPool);
        mapTileRepository = new MapTileRepositoryImpl(testConnectionPool);
        actionLogRepository = new ActionLogRepositoryImpl(testConnectionPool);

        treasureService = new TreasureServiceImpl(treasureRepository);
    }

    @Test
    void shouldSuccessfullyApplyPersonalMapModification() {
        Player player = Player.builder()
                .username("BuilderHero")
                .email("builder@rpg.com")
                .passwordHash("password")
                .x(10).y(10)
                .gold(100).energy(50).currentDay(1)
                .build();
        playerRepository.save(player);
        assertNotNull(player.getId(), "Гравець має успішно зберегтися та отримати ID");

        mapTileRepository.saveModification(player.getId(), 10, 10, "City");

        var modifications = mapTileRepository.findModificationsByPlayerId(player.getId());
        assertEquals(1, modifications.size(), "Має бути рівно одна модифікація");
        assertEquals("City", modifications.get(0).getNewTerrainType(), "Новий тип тайлу має бути City");
        assertEquals(10, modifications.get(0).getX());
        assertEquals(10, modifications.get(0).getY());
    }

    @Test
    void shouldCollectTreasureAndTriggerVictoryCondition() {
        Player player = Player.builder()
                .username("HunterHero")
                .email("hunter@rpg.com")
                .passwordHash("password")
                .x(5).y(5)
                .gold(50).energy(100).currentDay(1)
                .build();
        playerRepository.save(player);

        try (Connection conn = testDataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO TREASURE_TEMPLATE (X, Y, MIN_GOLD, MAX_GOLD) VALUES (5, 5, 10, 20)");
        } catch (SQLException e) {
            fail("Не вдалося додати скарб до тестової БД: " + e.getMessage());
        }

        List<TreasureDto> treasuresBefore = treasureService.findAllForPlayer(player.getId());
        assertEquals(1, treasuresBefore.size(), "На мапі має бути один скарб");
        assertFalse(treasuresBefore.get(0).getIsCollected(), "Скарб має бути незібраним спочатку");

        int treasureId = treasuresBefore.get(0).getId();
        treasureService.collectTreasure(player.getId(), treasureId);

        List<TreasureDto> treasuresAfter = treasureService.findAllForPlayer(player.getId());
        assertTrue(treasuresAfter.get(0).getIsCollected(), "Скарб має бути зібраним після виклику методу");

        boolean hasWon = treasureService.checkVictoryCondition(player.getId());
        assertTrue(hasWon, "Має спрацювати умова перемоги, оскільки всі наявні скарби зібрано");
    }

    @Test
    void shouldAuditPlayerActionsInLog() {
        // Arrange: Створюємо гравця
        Player player = Player.builder()
                .username("AuditHero")
                .email("audit@rpg.com")
                .passwordHash("password")
                .x(0).y(0)
                .gold(10).energy(10).currentDay(1)
                .build();
        playerRepository.save(player);

        // Act: Логуємо дію переміщення
        ActionLog moveLog = ActionLog.builder()
                .playerId(player.getId())
                .actionType("MOVE")
                .fromX(0).fromY(0)
                .toX(0).toY(1)
                .isValid(true)
                .createdAt(LocalDateTime.now())
                .build();
        actionLogRepository.save(moveLog);

        // Act: Логуємо дію безпечного відпочинку
        ActionLog restLog = ActionLog.builder()
                .playerId(player.getId())
                .actionType("TAVERN_REST")
                .fromX(0).fromY(1)
                .toX(0).toY(1)
                .isValid(true)
                .createdAt(LocalDateTime.now())
                .build();
        actionLogRepository.save(restLog);

        // Assert: Перевіряємо аудит логів у базі даних
        List<ActionLog> logs = actionLogRepository.findByPlayerId(player.getId());
        assertEquals(2, logs.size(), "У логах дій має бути зафіксовано 2 записи");

        // Зворотний хронологічний порядок у репозиторії
        assertEquals("TAVERN_REST", logs.get(0).getActionType(), "Перший лог має бути TAVERN_REST");
        assertEquals("MOVE", logs.get(1).getActionType(), "Другий лог має бути MOVE");

        assertEquals(0, logs.get(1).getFromX());
        assertEquals(0, logs.get(1).getFromY());
        assertEquals(0, logs.get(1).getToX());
        assertEquals(1, logs.get(1).getToY());
    }
}
