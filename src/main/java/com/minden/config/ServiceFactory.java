package com.minden.config;

import java.sql.SQLException;

import com.minden.entity.ConnectionPool;
import com.minden.infrastructure.email.EmailServiceImpl;
import com.minden.infrastructure.hashing.BCryptHashingService;
import com.minden.repository.ActionLogRepository;
import com.minden.repository.ActionLogRepositoryImpl;
import com.minden.repository.EventRepository;
import com.minden.repository.EventRepositoryImpl;
import com.minden.repository.MapTileRepository;
import com.minden.repository.MapTileRepositoryImpl;
import com.minden.repository.PlayerRepository;
import com.minden.repository.PlayerRepositoryImpl;
import com.minden.repository.TreasureRepository;
import com.minden.repository.TreasureRepositoryImpl;
import com.minden.service.AuthServiceImpl;
import com.minden.service.AuthenticationService;
import com.minden.service.EventService;
import com.minden.service.EventServiceImpl;
import com.minden.service.MapService;
import com.minden.service.MapServiceImpl;
import com.minden.service.PlayerService;
import com.minden.service.PlayerServiceImpl;
import com.minden.service.TreasureService;
import com.minden.service.TreasureServiceImpl;

/**
 * Фабрика сервісів — реалізація патернів Singleton та Dependency Injection.
 * Єдина точка створення та доступу до всіх залежностей додатку.
 *
 * <p>
 * Використання: {@code ServiceFactory.getInstance().getAuthService()}
 */
public class ServiceFactory {

    private static ServiceFactory instance;

    private final ConnectionPool connectionPool;

    private final PlayerRepository playerRepository;
    private final EventRepository eventRepository;
    private final TreasureRepository treasureRepository;
    private final ActionLogRepository actionLogRepository;
    private final MapTileRepository mapTileRepository;

    private final AuthenticationService authService;
    private final PlayerService playerService;
    private final MapService mapService;
    private final EventService eventService;
    private final TreasureService treasureService;

    /**
     * Приватний конструктор (Singleton). Створює всі залежності у правильному
     * порядку: Pool → Repository → Service.
     */
    private ServiceFactory() throws SQLException {
        this.connectionPool = new ConnectionPool(5);

        this.playerRepository = new PlayerRepositoryImpl(connectionPool);
        this.eventRepository = new EventRepositoryImpl(connectionPool);
        this.treasureRepository = new TreasureRepositoryImpl(connectionPool);
        this.actionLogRepository = new ActionLogRepositoryImpl(connectionPool);
        this.mapTileRepository = new MapTileRepositoryImpl(connectionPool);

        this.authService = new AuthServiceImpl(
                playerRepository,
                new BCryptHashingService(),
                new EmailServiceImpl()
        );
        this.playerService = new PlayerServiceImpl(playerRepository);
        this.mapService = new MapServiceImpl(mapTileRepository);
        this.eventService = new EventServiceImpl(eventRepository);
        this.treasureService = new TreasureServiceImpl(treasureRepository);
    }

    /**
     * Отримує єдиний екземпляр фабрики (Singleton + Lazy Initialization).
     *
     * @return екземпляр ServiceFactory
     * @throws SQLException якщо не вдалося підключитися до БД
     */
    public static synchronized ServiceFactory getInstance() throws SQLException {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    public AuthenticationService getAuthService() {
        return authService;
    }

    public PlayerService getPlayerService() {
        return playerService;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }

    public EventRepository getEventRepository() {
        return eventRepository;
    }

    public EventService getEventService() {
        return eventService;
    }

    public TreasureService getTreasureService() {
        return treasureService;
    }

    public TreasureRepository getTreasureRepository() {
        return treasureRepository;
    }

    public ActionLogRepository getActionLogRepository() {
        return actionLogRepository;
    }

    public MapTileRepository getMapTileRepository() {
        return mapTileRepository;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public MapService getMapService() {
        return mapService;
    }
}
