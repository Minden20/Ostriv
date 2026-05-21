package com.minden.ui.controller;

import com.minden.config.ServiceFactory;
import com.minden.entity.MapTile;
import com.minden.entity.Treasure;
import com.minden.repository.TreasureRepository;
import com.minden.service.MapService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;

public class MapController {
    
    @FXML private ScrollPane scrollPane;
    @FXML private Canvas mapCanvas;

    private MapService mapService;
    
    // Розмір однієї клітинки на екрані (у пікселях)
    private static final int TILE_SIZE = 10;
    private static final int MAP_WIDTH = 100;
    private static final int MAP_HEIGHT = 100;

    @FXML
    public void initialize() {
        try {
            mapService = ServiceFactory.getInstance().getMapService();
            
            // Встановлюємо розмір полотна
            mapCanvas.setWidth(MAP_WIDTH * TILE_SIZE);
            mapCanvas.setHeight(MAP_HEIGHT * TILE_SIZE);
            
            // Малюємо карту
            drawMap();
            
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
    }
    
    private Color getColorForTerrain(String terrainType) {
        if (terrainType == null) return Color.BLACK;
        
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
