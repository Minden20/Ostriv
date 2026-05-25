package com.minden.ui.controller;

import java.util.List;

import com.minden.config.ServiceFactory;
import com.minden.entity.ActionLog;
import com.minden.entity.MapTile;
import com.minden.service.MapService;
import com.minden.service.PathFinder;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MapController {

    @FXML
    private StackPane mapRootPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Canvas mapCanvas;

    private MapService mapService;
    private Timeline movementTimeline;

    /**
     * Map renderer for canvas drawing.
     */
    private com.minden.ui.renderer.MapRenderer mapRenderer;

    /**
     * Current visual X coordinate of the player.
     */
    private double visualX = -1.0;

    /**
     * Current visual Y coordinate of the player.
     */
    private double visualY = -1.0;

    /**
     * Current facing direction of the player.
     */
    private String facingDirection = "DOWN";

    /**
     * Animation timer for the game loop.
     */
    private javafx.animation.AnimationTimer gameLoop;

    /**
     * Memory cache for the map tiles grid.
     */
    private MapTile[][] cachedGrid;

    /**
     * Memory cache for the active treasures list.
     */
    private List<com.minden.dto.TreasureDto> cachedTreasures;

    // Розмір однієї клітинки на екрані (у пікселях)
    private static final int TILE_SIZE = 50;
    private static final int MAP_WIDTH = 100;
    private static final int MAP_HEIGHT = 100;

    private final boolean[][] exploredTiles = new boolean[MAP_WIDTH][MAP_HEIGHT];

    /**
     * Loads the map tiles and treasures from the database into the memory cache.
     */
    private void loadCache() {
        try {
            var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
            if (currentUser == null) {
                return;
            }
            Integer playerId = currentUser.getId();

            List<MapTile> tiles = mapService.getMapForPlayer(playerId);
            if (tiles != null) {
                cachedGrid = new MapTile[MAP_WIDTH][MAP_HEIGHT];
                for (MapTile tile : tiles) {
                    cachedGrid[tile.getX()][tile.getY()] = tile;
                }
            }

            var treasureService = ServiceFactory.getInstance().getTreasureService();
            cachedTreasures = treasureService.findAllForPlayer(playerId);
        } catch (Exception e) {
            System.err.println("Failed to load map cache: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        try {
            mapRenderer = new com.minden.ui.renderer.MapRenderer();
            mapService = ServiceFactory.getInstance().getMapService();

            loadCache();

            gameLoop = new javafx.animation.AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateVisualPosition();
                    drawMap();
                }
            };
            gameLoop.start();

            // Встановлюємо розмір полотна
            mapCanvas.setWidth(MAP_WIDTH * TILE_SIZE);
            mapCanvas.setHeight(MAP_HEIGHT * TILE_SIZE);

            // Додаємо обробник кліків миші для переміщення
            mapCanvas.setOnMouseClicked(event -> {
                double mouseX = event.getX();
                double mouseY = event.getY();
                int targetX = (int) (mouseX / TILE_SIZE);
                int targetY = (int) (mouseY / TILE_SIZE);

                if (targetX >= 0 && targetX < MAP_WIDTH && targetY >= 0 && targetY < MAP_HEIGHT) {
                    movePlayerTo(targetX, targetY);
                }
            });

            var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
            if (currentUser != null) {
                Integer playerId = currentUser.getId();
                try {
                    var actionLogRepo = ServiceFactory.getInstance().getActionLogRepository();
                    List<ActionLog> logs = actionLogRepo.findByPlayerId(playerId);
                    if (logs != null) {
                        for (var log : logs) {
                            if (log.getToX() != null && log.getToY() != null) {
                                updateFogOfWar(log.getToX(), log.getToY());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Помилка завантаження історії туману війни з логів: " + e.getMessage());
                }
            }

            // Малюємо карту
            drawMap();

            // Блокуємо скрол мишкою/тачпадом
            scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.ANY, event -> event.consume());

            /** Фокусуємо камеру на гравцеві та малюємо видиму область після завершення ініціалізації */
            javafx.application.Platform.runLater(() -> {
                centerCameraOnPlayer();
                drawMap();
            });

        } catch (Exception e) {
            System.err.println("Помилка ініціалізації карти: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateFogOfWar(int playerX, int playerY) {
        int radius = 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int tx = playerX + dx;
                int ty = playerY + dy;

                if (tx >= 0 && tx < MAP_WIDTH && ty >= 0 && ty < MAP_HEIGHT) {
                    if (dx * dx + dy * dy <= 16) {
                        exploredTiles[tx][ty] = true;
                    }
                }
            }
        }
    }

    /**
     * Відображає карту на полотні з обчисленням видимої області.
     */
    private void drawMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();

        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || cachedGrid == null) {
            return;
        }

        boolean hasPlayer = false;
        int targetX = 0;
        int targetY = 0;

        if (currentUser.getX() != null && currentUser.getY() != null) {
            targetX = currentUser.getX();
            targetY = currentUser.getY();
            hasPlayer = true;
            updateFogOfWar(targetX, targetY);
        }

        /** Обчислюємо розміри видимої області ScrollPane */
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        if (viewportWidth <= 0) {
            viewportWidth = scrollPane.getWidth();
        }
        if (viewportHeight <= 0) {
            viewportHeight = scrollPane.getHeight();
        }
        if (viewportWidth <= 0) {
            viewportWidth = 800.0;
        }
        if (viewportHeight <= 0) {
            viewportHeight = 600.0;
        }

        double contentWidth = mapCanvas.getWidth();
        double contentHeight = mapCanvas.getHeight();

        /** Обчислюємо видиму область безпосередньо з поточних значень прокрутки ScrollPane */
        double hValue = scrollPane.getHvalue();
        double vValue = scrollPane.getVvalue();

        double viewX = 0.0;
        if (contentWidth > viewportWidth) {
            viewX = hValue * (contentWidth - viewportWidth);
        }
        double viewY = 0.0;
        if (contentHeight > viewportHeight) {
            viewY = vValue * (contentHeight - viewportHeight);
        }

        /** Обмежуємо видиму область у межах полотна карти */
        viewX = Math.max(0.0, Math.min(contentWidth - viewportWidth, viewX));
        viewY = Math.max(0.0, Math.min(contentHeight - viewportHeight, viewY));

        try {
            mapRenderer.drawMap(
                    gc,
                    mapCanvas.getWidth(),
                    mapCanvas.getHeight(),
                    viewX,
                    viewY,
                    viewportWidth,
                    viewportHeight,
                    cachedGrid,
                    exploredTiles,
                    cachedTreasures,
                    hasPlayer,
                    targetX,
                    targetY,
                    visualX,
                    visualY,
                    facingDirection
            );
        } catch (Exception e) {
            System.err.println("Помилка при рендерингу карти: " + e.getMessage());
        }
    }

    /**
     * Переміщує гравця до вказаних логічних координат.
     * Розрахунок шляху виконується у фоновому потоці.
     *
     * @param targetX цільова координата X
     * @param targetY цільова координата Y
     */
    private void movePlayerTo(int targetX, int targetY) {
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getX() == null || currentUser.getY() == null) {
            return;
        }

        if (currentUser.getEnergy() == null || currentUser.getEnergy() <= 0) {
            System.out.println("Рух неможливий: недостатньо енергії!");
            return;
        }

        int startX = currentUser.getX();
        int startY = currentUser.getY();
        if (startX == targetX && startY == targetY) {
            return;
        }

        try {
            if (movementTimeline != null) {
                movementTimeline.stop();
            }

            if (cachedGrid == null) {
                loadCache();
            }
            MapTile[][] grid = cachedGrid;

            MapTile startTile = grid[startX][startY];
            MapTile endTile = grid[targetX][targetY];

            if (startTile == null || endTile == null) {
                return;
            }

            var playerPosition = currentUser;
            if (endTile != null && playerPosition != null) {
                if ("Water".equalsIgnoreCase(endTile.getTerrainType())) {
                    System.out.println("Рух неможливий: обрана клітинка є водою!");
                    return;
                }
            }

            new Thread(() -> {
                try {
                    PathFinder pathFinder = new PathFinder();
                    List<String> pathCoords = pathFinder.findPath(grid, startTile, endTile);

                    if (pathCoords != null && !pathCoords.isEmpty()) {
                        javafx.application.Platform.runLater(() -> {
                            /** Запускаємо ігровий цикл для плавної анімації руху */
                            if (gameLoop != null) {
                                gameLoop.start();
                            }
                            startMovementTimeline(pathCoords, grid);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Помилка розрахунку шляху: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Помилка ініціалізації розрахунку шляху: " + e.getMessage());
        }
    }

    /**
     * Запускає таймлайн переміщення гравця по знайденому шляху.
     * Всі важкі операції з базою даних перенесено у фоновий потік.
     *
     * @param pathCoords список координат знайденого шляху
     * @param grid сітка тайлів карти
     */
    private void startMovementTimeline(List<String> pathCoords, MapTile[][] grid) {
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        movementTimeline = new Timeline();
        for (int i = 1; i < pathCoords.size(); i++) {
            final int stepIndex = i;
            String coord = pathCoords.get(i);
            String[] parts = coord.split(",");
            int nextX = Integer.parseInt(parts[0]);
            int nextY = Integer.parseInt(parts[1]);

            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(150 * stepIndex),
                    event -> {
                        MapTile nextTile = grid[nextX][nextY];
                        int stepCost = nextTile != null ? nextTile.getMovementCost() : 1;

                        if (currentUser.getEnergy() == null || currentUser.getEnergy() < stepCost) {
                            System.out.println("Рух зупинено: недостатньо енергії!");
                            if (movementTimeline != null) {
                                movementTimeline.stop();
                            }
                            return;
                        }

                        currentUser.setEnergy(currentUser.getEnergy() - stepCost);

                        int currentX = currentUser.getX();
                        int currentY = currentUser.getY();

                        currentUser.setX(nextX);
                        currentUser.setY(nextY);

                        drawMap();
                        centerCameraOnPlayer();

                        new Thread(() -> {
                            try {
                                var playerRepo = ServiceFactory.getInstance().getPlayerRepository();
                                playerRepo.findById(currentUser.getId()).ifPresent(player -> {
                                    player.setX(nextX);
                                    player.setY(nextY);
                                    player.setEnergy(currentUser.getEnergy());
                                    playerRepo.update(player);
                                });

                                if (com.minden.ui.controller.MainController.getInstance() != null) {
                                    javafx.application.Platform.runLater(() -> com.minden.ui.controller.MainController.getInstance().updatePlayerStats());
                                }

                                var actionLogRepo = ServiceFactory.getInstance().getActionLogRepository();
                                actionLogRepo.save(com.minden.entity.ActionLog.builder()
                                        .playerId(currentUser.getId())
                                        .actionType("MOVE")
                                        .fromX(currentX)
                                        .fromY(currentY)
                                        .toX(nextX)
                                        .toY(nextY)
                                        .isValid(true)
                                        .createdAt(java.time.LocalDateTime.now())
                                        .build());

                                var treasureService = ServiceFactory.getInstance().getTreasureService();
                                if (cachedTreasures == null) {
                                    cachedTreasures = treasureService.findAllForPlayer(currentUser.getId());
                                }
                                for (var treasure : cachedTreasures) {
                                    if (!treasure.getIsCollected() && treasure.getX() == nextX && treasure.getY() == nextY) {
                                        treasureService.collectTreasure(currentUser.getId(), treasure.getId());
                                        treasure.setIsCollected(true);

                                        int minGold = treasure.getMinGold() != null ? treasure.getMinGold() : 0;
                                        int maxGold = treasure.getMaxGold() != null ? treasure.getMaxGold() : 0;
                                        int rewardedGold = minGold + (int) (Math.random() * ((maxGold - minGold) + 1));

                                        currentUser.setGold(currentUser.getGold() + rewardedGold);

                                        playerRepo.findById(currentUser.getId()).ifPresent(player -> {
                                            player.setGold(currentUser.getGold());
                                            playerRepo.update(player);
                                        });

                                        if (com.minden.ui.controller.MainController.getInstance() != null) {
                                            javafx.application.Platform.runLater(() -> com.minden.ui.controller.MainController.getInstance().updatePlayerStats());
                                        }

                                        actionLogRepo.save(com.minden.entity.ActionLog.builder()
                                                .playerId(currentUser.getId())
                                                .actionType("COLLECT_TREASURE")
                                                .fromX(nextX)
                                                .fromY(nextY)
                                                .toX(nextX)
                                                .toY(nextY)
                                                .isValid(true)
                                                .createdAt(java.time.LocalDateTime.now())
                                                .build());

                                        if (treasureService.checkVictoryCondition(currentUser.getId())) {
                                            javafx.application.Platform.runLater(() -> showVictoryOverlay());
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                System.err.println("Помилка фонового збереження ходу: " + ex.getMessage());
                            }
                        }).start();
                    }
            );
            movementTimeline.getKeyFrames().add(keyFrame);
        }
        movementTimeline.play();
    }

    /**
     * Updates the player's smooth visual position and determines movement
     * direction.
     */
    private void updateVisualPosition() {
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getX() == null || currentUser.getY() == null) {
            return;
        }

        double targetX = currentUser.getX();
        double targetY = currentUser.getY();

        if (visualX == -1.0 || visualY == -1.0) {
            visualX = targetX;
            visualY = targetY;
        }

        double dx = targetX - visualX;
        double dy = targetY - visualY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.01) {
            double speed = 0.12;
            visualX += dx * speed;
            visualY += dy * speed;

            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0) {
                    facingDirection = "RIGHT";
                } else {
                    facingDirection = "LEFT";
                }
            } else {
                if (dy > 0) {
                    facingDirection = "DOWN";
                } else {
                    facingDirection = "UP";
                }
            }
            /** Оновлюємо камеру синхронно для запобігання дьоргання */
            centerCameraOnPlayer();
        } else {
            visualX = targetX;
            visualY = targetY;
            /** Перевіряємо, чи активний таймлайн переміщення в даний момент */
            boolean isTimelineRunning = (movementTimeline != null && 
                    movementTimeline.getStatus() == javafx.animation.Animation.Status.RUNNING);
            /** Зупиняємо ігровий цикл, лише якщо таймлайн завершився і персонаж стоїть на місці */
            if (!isTimelineRunning) {
                if (gameLoop != null) {
                    gameLoop.stop();
                }
            }
        }
    }

    private void centerCameraOnPlayer() {
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getX() == null || currentUser.getY() == null) {
            return;
        }

        double px = visualX != -1.0 ? visualX : currentUser.getX();
        double py = visualY != -1.0 ? visualY : currentUser.getY();

        double playerPixelX = px * TILE_SIZE + TILE_SIZE / 2.0;
        double playerPixelY = py * TILE_SIZE + TILE_SIZE / 2.0;

        double contentWidth = mapCanvas.getWidth();
        double contentHeight = mapCanvas.getHeight();

        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        // Якщо в'юпорт ще не відмальований або має нульову ширину/висоту, використовуємо розміри scrollPane
        if (viewportWidth <= 0) {
            viewportWidth = scrollPane.getWidth();
        }
        if (viewportHeight <= 0) {
            viewportHeight = scrollPane.getHeight();
        }

        double hMax = scrollPane.getHmax();
        double hMin = scrollPane.getHmin();
        double vMax = scrollPane.getVmax();
        double vMin = scrollPane.getVmin();

        // Розраховуємо hvalue
        if (contentWidth > viewportWidth) {
            double hValue = (playerPixelX - viewportWidth / 2.0) / (contentWidth - viewportWidth);
            hValue = Math.max(hMin, Math.min(hMax, hValue));
            scrollPane.setHvalue(hValue);
        }

        // Розраховуємо vvalue
        if (contentHeight > viewportHeight) {
            double vValue = (playerPixelY - viewportHeight / 2.0) / (contentHeight - viewportHeight);
            vValue = Math.max(vMin, Math.min(vMax, vValue));
            scrollPane.setVvalue(vValue);
        }
    }



    @FXML
    private void handleRest() {
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        try {
            List<MapTile> tiles = mapService.getMapForPlayer(currentUser.getId());
            MapTile[][] grid = new MapTile[MAP_WIDTH][MAP_HEIGHT];
            for (MapTile tile : tiles) {
                grid[tile.getX()][tile.getY()] = tile;
            }

            int px = currentUser.getX();
            int py = currentUser.getY();
            MapTile currentTile = grid[px][py];
            boolean isInCity = currentTile != null && "City".equalsIgnoreCase(currentTile.getTerrainType());

            showRestChoiceOverlay(isInCity);
        } catch (Exception e) {
            System.err.println("Помилка при перевірці відпочинку: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showRestChoiceOverlay(boolean isInCity) {
        if (mapRootPane == null) {
            return;
        }

        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        int playerGold = currentUser.getGold() != null ? currentUser.getGold() : 0;

        StackPane overlay = com.minden.ui.dialog.MapDialogFactory.createRestChoiceOverlay(
                isInCity,
                playerGold,
                isSafeRest -> {
                    mapRootPane.getChildren().remove(mapRootPane.getChildren().size() - 1);
                    if (isSafeRest) {
                        currentUser.setGold(playerGold - 15);
                    }
                    processRest(isSafeRest);
                },
                () -> mapRootPane.getChildren().remove(mapRootPane.getChildren().size() - 1)
        );

        mapRootPane.getChildren().add(overlay);
    }

    private void processRest(boolean isSafeRest) {
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        try {
            var playerRepo = ServiceFactory.getInstance().getPlayerRepository();

            int nextDay = (currentUser.getCurrentDay() != null ? currentUser.getCurrentDay() : 1) + 1;
            currentUser.setCurrentDay(nextDay);

            int oldEnergy = currentUser.getEnergy() != null ? currentUser.getEnergy() : 0;
            int restoredEnergyAmount = isSafeRest ? 100 : 50;
            int newEnergy = Math.min(100, oldEnergy + restoredEnergyAmount);
            int actuallyRestored = newEnergy - oldEnergy;
            currentUser.setEnergy(newEnergy);

            playerRepo.findById(currentUser.getId()).ifPresent(player -> {
                player.setEnergy(newEnergy);
                player.setGold(currentUser.getGold());
                player.setCurrentDay(nextDay);
                playerRepo.update(player);
            });

            String eventName = "Спокійний відпочинок";
            String eventDescription = "Ви чудово відпочили біля багаття під зоряним небом. Навколо тихо і спокійно.";
            int goldLost = 0;

            if (isSafeRest) {
                eventName = "Затишна ніч у таверні";
                eventDescription = "Ви смачно повечеряли та міцно виспалися на м'якому ліжку таверни. Жодних пригод цієї ночі не сталося.";
            } else {
                var eventRepo = ServiceFactory.getInstance().getEventRepository();
                List<com.minden.entity.Event> events = eventRepo.findAll();

                if (events != null && !events.isEmpty()) {
                    int randomIndex = (int) (Math.random() * events.size());
                    com.minden.entity.Event randomEvent = events.get(randomIndex);

                    eventName = randomEvent.getName();
                    eventDescription = randomEvent.getDescription();

                    int minPenalty = randomEvent.getMinGoldPenalty() != null ? randomEvent.getMinGoldPenalty() : 0;
                    int maxPenalty = randomEvent.getMaxGoldPenalty() != null ? randomEvent.getMaxGoldPenalty() : 0;
                    goldLost = minPenalty + (int) (Math.random() * ((maxPenalty - minPenalty) + 1));

                    int oldGold = currentUser.getGold() != null ? currentUser.getGold() : 0;
                    int newGold = Math.max(0, oldGold - goldLost);
                    currentUser.setGold(newGold);

                    playerRepo.findById(currentUser.getId()).ifPresent(player -> {
                        player.setGold(newGold);
                        playerRepo.update(player);
                    });

                    playerRepo.addEventToHistory(currentUser.getId(), randomEvent.getId(), nextDay);
                }
            }

            var actionLogRepo = ServiceFactory.getInstance().getActionLogRepository();
            actionLogRepo.save(com.minden.entity.ActionLog.builder()
                    .playerId(currentUser.getId())
                    .actionType(isSafeRest ? "TAVERN_REST" : "WILD_REST")
                    .fromX(currentUser.getX())
                    .fromY(currentUser.getY())
                    .toX(currentUser.getX())
                    .toY(currentUser.getY())
                    .isValid(true)
                    .createdAt(java.time.LocalDateTime.now())
                    .build());

            if (MainController.getInstance() != null) {
                MainController.getInstance().updatePlayerStats();
            }

            showEventOverlay(eventName, eventDescription, goldLost, actuallyRestored);

        } catch (Exception e) {
            System.err.println("Помилка під час обробки відпочинку: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showEventOverlay(String title, String description, int goldLost, int energyRestored) {
        if (mapRootPane == null) {
            return;
        }

        StackPane overlay = com.minden.ui.dialog.MapDialogFactory.createEventOverlay(
                title,
                description,
                goldLost,
                energyRestored,
                () -> mapRootPane.getChildren().remove(mapRootPane.getChildren().size() - 1)
        );

        mapRootPane.getChildren().add(overlay);
    }

    private void showVictoryOverlay() {
        if (mapRootPane == null) {
            return;
        }

        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        StackPane overlay = com.minden.ui.dialog.MapDialogFactory.createVictoryOverlay(
                currentUser.getUsername(),
                currentUser.getCurrentDay(),
                currentUser.getGold(),
                () -> {
                    mapRootPane.getChildren().remove(mapRootPane.getChildren().size() - 1);
                    com.minden.ui.SessionContext.getInstance().logout();
                    com.minden.ui.JavaFxApp.setRoot("login");
                }
        );

        mapRootPane.getChildren().add(overlay);
    }
}
