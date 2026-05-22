package com.minden.ui.controller;

import java.util.List;

import com.minden.config.ServiceFactory;
import com.minden.entity.MapTile;
import com.minden.service.MapService;
import com.minden.service.PathFinder;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MapController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Canvas mapCanvas;

    private MapService mapService;
    private Timeline movementTimeline;

    // Розмір однієї клітинки на екрані (у пікселях)
    private static final int TILE_SIZE = 50;
    private static final int MAP_WIDTH = 100;
    private static final int MAP_HEIGHT = 100;

    @FXML
    public void initialize() {
        try {
            mapService = ServiceFactory.getInstance().getMapService();

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

            // Малюємо карту
            drawMap();

            // Блокуємо скрол мишкою/тачпадом
            scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.ANY, event -> event.consume());

            // Фокусуємо камеру на гравцеві після завантаження
            javafx.application.Platform.runLater(this::centerCameraOnPlayer);

        } catch (Exception e) {
            System.err.println("Помилка ініціалізації карти: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void drawMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();

        // Очищаємо фон
        gc.setFill(Color.web("#1e1e2e"));
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        // Завантажуємо тайли з бази (це працює швидко, бо їх всього 10к)
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        Integer playerId = currentUser != null ? currentUser.getId() : null;
        List<MapTile> tiles = mapService.getMapForPlayer(playerId);

        if (tiles == null || tiles.isEmpty()) {
            gc.setFill(Color.WHITE);
            gc.fillText("Карта не знайдена в БД. Перевірте консоль на помилки імпорту.", 50, 50);
            return;
        }

        // Малюємо кожен тайл
        for (MapTile tile : tiles) {
            Color color = getColorForTerrain(tile.getTerrainType());
            gc.setFill(color);
            gc.fillRect(tile.getX() * TILE_SIZE, tile.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // Малюємо сітку для зручності
        gc.setStroke(Color.web("#313244", 0.5));
        gc.setLineWidth(0.5);
        for (int x = 0; x <= MAP_WIDTH; x++) {
            gc.strokeLine(x * TILE_SIZE, 0, x * TILE_SIZE, MAP_HEIGHT * TILE_SIZE);
        }
        for (int y = 0; y <= MAP_HEIGHT; y++) {
            gc.strokeLine(0, y * TILE_SIZE, MAP_WIDTH * TILE_SIZE, y * TILE_SIZE);
        }

        // Відображення скарбів
        try {
            var treasureService = ServiceFactory.getInstance().getTreasureService();
            List<com.minden.dto.TreasureDto> treasures = treasureService.findAllForPlayer(playerId);

            gc.setFill(Color.web("#8B4513")); // Коричневий колір для скарбів

            for (var treasure : treasures) {
                if (!treasure.getIsCollected()) {
                    // Малюємо коло, трохи менше за клітинку
                    double padding = 2.0;
                    gc.fillOval(
                            treasure.getX() * TILE_SIZE + padding,
                            treasure.getY() * TILE_SIZE + padding,
                            TILE_SIZE - padding * 2,
                            TILE_SIZE - padding * 2
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження скарбів: " + e.getMessage());
        }

        // Відображення гравця
        if (currentUser != null && currentUser.getX() != null && currentUser.getY() != null) {
            gc.setFill(Color.web("#f38ba8")); // Beautiful pastel pink/red
            double padding = 1.0;
            double size = TILE_SIZE - padding * 2;
            gc.fillOval(
                    currentUser.getX() * TILE_SIZE + padding,
                    currentUser.getY() * TILE_SIZE + padding,
                    size,
                    size
            );
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.0);
            gc.strokeOval(
                    currentUser.getX() * TILE_SIZE + padding,
                    currentUser.getY() * TILE_SIZE + padding,
                    size,
                    size
            );
        }
    }

    private void movePlayerTo(int targetX, int targetY) {
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getX() == null || currentUser.getY() == null) {
            return;
        }

        int startX = currentUser.getX();
        int startY = currentUser.getY();
        if (startX == targetX && startY == targetY) {
            return;
        }

        try {
            // Зупиняємо попереднє переміщення, якщо воно триває
            if (movementTimeline != null) {
                movementTimeline.stop();
            }

            // Отримуємо тайли карти
            List<MapTile> tiles = mapService.getMapForPlayer(currentUser.getId());
            MapTile[][] grid = new MapTile[MAP_WIDTH][MAP_HEIGHT];
            for (MapTile tile : tiles) {
                grid[tile.getX()][tile.getY()] = tile;
            }

            MapTile startTile = grid[startX][startY];
            MapTile endTile = grid[targetX][targetY];

            if (startTile == null || endTile == null) {
                return;
            }

            // Обчислюємо шлях за допомогою PathFinder
            PathFinder pathFinder = new PathFinder();
            List<String> pathCoords = pathFinder.findPath(grid, startTile, endTile);

            if (pathCoords != null && !pathCoords.isEmpty()) {
                movementTimeline = new Timeline();

                // pathCoords містить початкову точку на індексі 0, тому починаємо з 1
                for (int i = 1; i < pathCoords.size(); i++) {
                    final int stepIndex = i;
                    String coord = pathCoords.get(i);
                    String[] parts = coord.split(",");
                    int nextX = Integer.parseInt(parts[0]);
                    int nextY = Integer.parseInt(parts[1]);

                    KeyFrame keyFrame = new KeyFrame(
                            Duration.millis(150 * stepIndex),
                            event -> {
                                int currentX = currentUser.getX();
                                int currentY = currentUser.getY();

                                // Оновлюємо координати гравця
                                currentUser.setX(nextX);
                                currentUser.setY(nextY);

                                // Перемальовуємо карту
                                drawMap();

                                // Фокусуємо камеру на гравцеві
                                centerCameraOnPlayer();

                                // Оновлюємо позицію в базі даних та додаємо лог руху
                                try {
                                    var playerRepo = ServiceFactory.getInstance().getPlayerRepository();
                                    playerRepo.findById(currentUser.getId()).ifPresent(player -> {
                                        player.setX(nextX);
                                        player.setY(nextY);
                                        playerRepo.update(player);
                                    });

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

                                    // Перевіряємо збирання скарбів на цій клітинці
                                    var treasureService = ServiceFactory.getInstance().getTreasureService();
                                    List<com.minden.dto.TreasureDto> treasures = treasureService.findAllForPlayer(currentUser.getId());
                                    for (var treasure : treasures) {
                                        if (!treasure.getIsCollected() && treasure.getX() == nextX && treasure.getY() == nextY) {
                                            treasureService.collectTreasure(currentUser.getId(), treasure.getId());

                                            int minGold = treasure.getMinGold() != null ? treasure.getMinGold() : 0;
                                            int maxGold = treasure.getMaxGold() != null ? treasure.getMaxGold() : 0;
                                            int rewardedGold = minGold + (int) (Math.random() * ((maxGold - minGold) + 1));

                                            currentUser.setGold(currentUser.getGold() + rewardedGold);

                                            playerRepo.findById(currentUser.getId()).ifPresent(player -> {
                                                player.setGold(currentUser.getGold());
                                                playerRepo.update(player);
                                            });

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
                                        }
                                    }

                                } catch (Exception ex) {
                                    System.err.println("Помилка при оновленні переміщення: " + ex.getMessage());
                                    ex.printStackTrace();
                                }
                            }
                    );
                    movementTimeline.getKeyFrames().add(keyFrame);
                }
                movementTimeline.play();
            }

        } catch (Exception e) {
            System.err.println("Помилка розрахунку шляху: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void centerCameraOnPlayer() {
        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getX() == null || currentUser.getY() == null) {
            return;
        }

        double playerPixelX = currentUser.getX() * TILE_SIZE + TILE_SIZE / 2.0;
        double playerPixelY = currentUser.getY() * TILE_SIZE + TILE_SIZE / 2.0;

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

    private Color getColorForTerrain(String terrainType) {
        if (terrainType == null) {
            return Color.BLACK;
        }

        switch (terrainType) {
            case "Water":
                return Color.web("#89b4fa"); // Синій (Water)
            case "Forest":
                return Color.web("#a6e3a1"); // Зелений (Forest)
            case "Sand":
                return Color.web("#f9e2af"); // Жовтуватий (Sand)
            default:
                return Color.GRAY; // Невідомий тип
        }
    }
}
