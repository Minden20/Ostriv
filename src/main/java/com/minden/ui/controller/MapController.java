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

    // Розмір однієї клітинки на екрані (у пікселях)
    private static final int TILE_SIZE = 50;
    private static final int MAP_WIDTH = 100;
    private static final int MAP_HEIGHT = 100;

    private final boolean[][] exploredTiles = new boolean[MAP_WIDTH][MAP_HEIGHT];

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

            // Фокусуємо камеру на гравцеві після завантаження
            javafx.application.Platform.runLater(this::centerCameraOnPlayer);

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

    private void drawMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#18110b"));
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        Integer playerId = currentUser != null ? currentUser.getId() : null;
        List<MapTile> tiles = mapService.getMapForPlayer(playerId);

        if (tiles == null || tiles.isEmpty()) {
            gc.setFill(Color.WHITE);
            gc.fillText("Карта не знайдена в БД. Перевірте консоль на помилки імпорту.", 50, 50);
            return;
        }

        MapTile[][] grid = new MapTile[MAP_WIDTH][MAP_HEIGHT];
        for (MapTile tile : tiles) {
            grid[tile.getX()][tile.getY()] = tile;
        }

        int px = 0;
        int py = 0;
        boolean hasPlayer = false;

        if (currentUser != null && currentUser.getX() != null && currentUser.getY() != null) {
            px = currentUser.getX();
            py = currentUser.getY();
            hasPlayer = true;
            updateFogOfWar(px, py);
        }

        gc.setLineWidth(0.5);
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                if (!exploredTiles[x][y]) {
                    continue; // Закриті туманом тайли навіть не намагаємося рендерити!
                }

                MapTile tile = grid[x][y];
                if (tile == null) {
                    continue;
                }

                boolean isVisibleNow = false;
                if (hasPlayer) {
                    int dx = x - px;
                    int dy = y - py;
                    isVisibleNow = (dx * dx + dy * dy <= 16);
                }

                Color terrainColor = getColorForTerrain(tile.getTerrainType());

                if (isVisibleNow) {
                    gc.setFill(terrainColor);
                } else {
                    gc.setFill(terrainColor.deriveColor(0, 1.0, 0.40, 1.0));
                }

                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                gc.setStroke(Color.web("#5e4531", isVisibleNow ? 0.35 : 0.15));
                gc.strokeRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        try {
            var treasureService = ServiceFactory.getInstance().getTreasureService();
            List<com.minden.dto.TreasureDto> treasures = treasureService.findAllForPlayer(playerId);

            for (var treasure : treasures) {
                if (!treasure.getIsCollected()) {
                    int tx = treasure.getX();
                    int ty = treasure.getY();

                    if (hasPlayer) {
                        int dx = tx - px;
                        int dy = ty - py;
                        if (dx * dx + dy * dy <= 16) {
                            gc.setFill(Color.web("#8B4513"));
                            double padding = 6.0;
                            gc.fillOval(
                                    tx * TILE_SIZE + padding,
                                    ty * TILE_SIZE + padding,
                                    TILE_SIZE - padding * 2,
                                    TILE_SIZE - padding * 2
                            );
                            gc.setStroke(Color.web("#1e1e2e"));
                            gc.setLineWidth(1.0);
                            gc.strokeOval(
                                    tx * TILE_SIZE + padding,
                                    ty * TILE_SIZE + padding,
                                    TILE_SIZE - padding * 2,
                                    TILE_SIZE - padding * 2
                            );
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження скарбів: " + e.getMessage());
        }

        if (hasPlayer) {
            gc.setFill(Color.web("#c63d2f")); // Cozy crimson wax seal / campfire red
            double padding = 2.0;
            double size = TILE_SIZE - padding * 2;
            gc.fillOval(
                    px * TILE_SIZE + padding,
                    py * TILE_SIZE + padding,
                    size,
                    size
            );
            gc.setStroke(Color.web("#dfaf64")); // Golden outline
            gc.setLineWidth(1.5);
            gc.strokeOval(
                    px * TILE_SIZE + padding,
                    py * TILE_SIZE + padding,
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

        // Перевіряємо, чи є енергія для початку руху
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

            var playerPosition = currentUser;
            if (endTile != null && playerPosition != null) {
                if ("Water".equalsIgnoreCase(endTile.getTerrainType())) {
                    System.out.println("Рух неможливий: обрана клітинка є водою!");
                    return;
                }
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
                                // Визначаємо вартість переміщення на наступний тайл
                                MapTile nextTile = grid[nextX][nextY];
                                int stepCost = nextTile != null ? nextTile.getMovementCost() : 1;

                                // Перевіряємо енергію перед кожним кроком з урахуванням вартості тайлу
                                if (currentUser.getEnergy() == null || currentUser.getEnergy() < stepCost) {
                                    System.out.println("Рух зупинено: недостатньо енергії для кроку на "
                                            + (nextTile != null ? nextTile.getTerrainType() : "тайл") + " (потрібно " + stepCost + ")!");
                                    if (movementTimeline != null) {
                                        movementTimeline.stop();
                                    }
                                    return;
                                }

                                // Зменшуємо енергію на вартість переміщення тайлу
                                currentUser.setEnergy(currentUser.getEnergy() - stepCost);

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
                                        player.setEnergy(currentUser.getEnergy());
                                        playerRepo.update(player);
                                    });

                                    // Оновлюємо відображення характеристик на головній панелі
                                    if (com.minden.ui.controller.MainController.getInstance() != null) {
                                        com.minden.ui.controller.MainController.getInstance().updatePlayerStats();
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

                                            // Миттєво оновлюємо нове золото на екрані
                                            if (com.minden.ui.controller.MainController.getInstance() != null) {
                                                com.minden.ui.controller.MainController.getInstance().updatePlayerStats();
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
                return Color.web("#4b779a"); // Глибокий річковий синій (Water)
            case "Forest":
                return Color.web("#4b6d4c"); // Густий лісовий зелений (Forest)
            case "Sand":
                return Color.web("#ebd8b0"); // Теплий піщаний / паперовий (Sand)
            case "City":
                return Color.web("#c87a53"); // Затишний теракотовий (City)
            default:
                return Color.GRAY; // Невідомий тип
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

        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(26, 18, 12, 0.85); -fx-alignment: center;");

        VBox dialogBox = new VBox(20);
        dialogBox.setStyle("-fx-background-color: #faf6ec; "
                + "-fx-border-color: #a67c52; "
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px; "
                + "-fx-padding: 30px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(27,20,14,0.4), 15, 0, 0, 8);");
        dialogBox.setMaxSize(480, 350);
        dialogBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("⛺ Вибір місця для ночівлі");
        titleLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #8c3b2b;");

        Label descLabel = new Label(isInCity
                ? "Ви перебуваєте в безпечних стінах міста. Де ви бажаєте заночувати?"
                : "Навколо лише дика природа та небезпеки. Ви можете розбити табір тут безкоштовно, але це ризиковано.");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-text-fill: #3d2612; -fx-text-alignment: center;");

        VBox buttonsBox = new VBox(12);
        buttonsBox.setAlignment(Pos.CENTER);

        Button wildRestBtn = new Button(isInCity ? "Спати на вулиці міста (Безкоштовно, небезпечно)" : "Розбити табір (Безкоштовно, небезпечно)");
        wildRestBtn.setStyle("-fx-background-color: #c66347; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;");
        wildRestBtn.setOnAction(e -> {
            mapRootPane.getChildren().remove(backdrop);
            processRest(false); // false = небезпечний відпочинок
        });

        wildRestBtn.setOnMouseEntered(e -> wildRestBtn.setStyle("-fx-background-color: #d9785c; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;"));
        wildRestBtn.setOnMouseExited(e -> wildRestBtn.setStyle("-fx-background-color: #c66347; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;"));

        buttonsBox.getChildren().add(wildRestBtn);

        if (isInCity) {
            int tavernCost = 15;
            Button tavernRestBtn = new Button("Орендувати кімнату в Таверні (💰 " + tavernCost + " Золота, безпечно)");
            tavernRestBtn.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;");

            tavernRestBtn.setOnAction(e -> {
                var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
                if (currentUser.getGold() >= tavernCost) {
                    mapRootPane.getChildren().remove(backdrop);
                    currentUser.setGold(currentUser.getGold() - tavernCost);
                    processRest(true); // true = безпечний відпочинок
                } else {
                    descLabel.setText("❌ У вас недостатньо золота для кімнати в таверні!");
                    descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b84f3d; -fx-text-alignment: center;");
                }
            });

            tavernRestBtn.setOnMouseEntered(e -> tavernRestBtn.setStyle("-fx-background-color: #57825e; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;"));
            tavernRestBtn.setOnMouseExited(e -> tavernRestBtn.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand; -fx-pref-width: 420px;"));

            buttonsBox.getChildren().add(tavernRestBtn);
        }

        Button cancelBtn = new Button("Назад");
        cancelBtn.setStyle("-fx-background-color: #e8dfcd; -fx-text-fill: #3d2612; -fx-font-family: 'Georgia'; -fx-font-size: 13px; -fx-padding: 8px 16px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> mapRootPane.getChildren().remove(backdrop));

        dialogBox.getChildren().addAll(titleLabel, descLabel, buttonsBox, cancelBtn);
        backdrop.getChildren().add(dialogBox);
        mapRootPane.getChildren().add(backdrop);
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

            // Записуємо лог дії в ACTION_LOG
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

        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(26, 18, 12, 0.85); -fx-alignment: center;");

        VBox dialogBox = new VBox(20);
        dialogBox.setStyle("-fx-background-color: #faf6ec; -fx-border-color: #a67c52; -fx-border-width: 2px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 30px; -fx-effect: dropshadow(three-pass-box, rgba(27,20,14,0.4), 15, 0, 0, 8);");
        dialogBox.setMaxSize(450, 320);
        dialogBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("🔥 Подія: " + title);
        titleLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #8c3b2b;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-text-fill: #3d2612; -fx-alignment: center; -fx-text-alignment: center;");

        HBox statsBox = new HBox(25);
        statsBox.setAlignment(Pos.CENTER);

        Label energyDiff = new Label("⚡ +" + energyRestored + " Енергії");
        energyDiff.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b779a;");

        Label goldDiff = new Label(goldLost > 0 ? "💰 -" + goldLost + " Золота" : "💰 Без втрат");
        goldDiff.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + (goldLost > 0 ? "#b84f3d;" : "#4b7252;"));

        statsBox.getChildren().addAll(energyDiff, goldDiff);

        Button closeButton = new Button("Продовжити подорож");
        closeButton.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;");
        closeButton.setOnAction(e -> mapRootPane.getChildren().remove(backdrop));

        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #57825e; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));

        dialogBox.getChildren().addAll(titleLabel, descLabel, statsBox, closeButton);
        backdrop.getChildren().add(dialogBox);

        mapRootPane.getChildren().add(backdrop);
    }

    private void showVictoryOverlay() {
        if (mapRootPane == null) {
            return;
        }

        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(26, 18, 12, 0.9); -fx-alignment: center;");

        VBox victoryBox = new VBox(25);
        victoryBox.setStyle("-fx-background-color: #faf6ec; "
                + "-fx-border-color: #c5a059; "
                + "-fx-border-width: 3px; "
                + "-fx-border-radius: 16px; "
                + "-fx-background-radius: 16px; "
                + "-fx-padding: 40px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(140,94,56,0.3), 20, 0, 0, 0);");
        victoryBox.setMaxSize(500, 380);
        victoryBox.setAlignment(Pos.CENTER);

        Label trophyLabel = new Label("🏆");
        trophyLabel.setStyle("-fx-font-size: 64px;");

        Label titleLabel = new Label("ВЕЛИКА ПЕРЕМОГА!");
        titleLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #8c3b2b;");

        var currentUser = com.minden.ui.SessionContext.getInstance().getCurrentUser();
        String victoryText = String.format(
                "Вітаємо, %s!\nВи знайшли всі приховані скарби на острові!\n"
                + "Ви витратили: %d днів (ходів).\n"
                + "Ваше фінальне золото: 💰 %d",
                currentUser.getUsername(), currentUser.getCurrentDay(), currentUser.getGold()
        );

        Label descLabel = new Label(victoryText);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16px; -fx-text-fill: #3d2612; -fx-text-alignment: center; -fx-line-spacing: 5px;");

        Button exitBtn = new Button("Повернутися в меню");
        exitBtn.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 12px 25px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> {
            mapRootPane.getChildren().remove(backdrop);
            com.minden.ui.SessionContext.getInstance().logout();
            com.minden.ui.JavaFxApp.setRoot("login");
        });

        exitBtn.setOnMouseEntered(ev -> exitBtn.setStyle("-fx-background-color: #57825e; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 12px 25px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));
        exitBtn.setOnMouseExited(ev -> exitBtn.setStyle("-fx-background-color: #4b7252; -fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 12px 25px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"));

        victoryBox.getChildren().addAll(trophyLabel, titleLabel, descLabel, exitBtn);
        backdrop.getChildren().add(victoryBox);
        mapRootPane.getChildren().add(backdrop);
    }
}
