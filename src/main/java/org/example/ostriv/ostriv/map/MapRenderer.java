package org.example.ostriv.ostriv.map;

import org.example.ostriv.ostriv.mobs.Player;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapRenderer {

    private static final int TILE_SIZE = 50;
    private Canvas canvas;

    /**
     * Ініціалізує Canvas відповідно до розмірів карти.
     */
    public Canvas createMapCanvas(MapModel mapModel) {
        // Рахуємо загальний розмір полотна в пікселях
        double width = mapModel.getWidth() * TILE_SIZE;
        double height = mapModel.getHeight() * TILE_SIZE;
        
        canvas = new Canvas(width, height);
        return canvas;
    }

    /**
     * Відмальовує всю карту та гравця за один раз.
     */
    public void renderAll(MapModel mapModel, Player player) {
        if (canvas == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Очищуємо попередній кадр (обов'язково перед новим малюванням)
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 2. Малюємо фон (сірі клітинки)
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Малюємо сітку (опціонально)
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);
        for (int y = 0; y <= mapModel.getHeight(); y++) {
            gc.strokeLine(0, y * TILE_SIZE, canvas.getWidth(), y * TILE_SIZE);
        }
        for (int x = 0; x <= mapModel.getWidth(); x++) {
            gc.strokeLine(x * TILE_SIZE, 0, x * TILE_SIZE, canvas.getHeight());
        }

        // 3. Малюємо тайли
        for (Tile tile : mapModel.getTiles()) {
            gc.setFill(getTileColor(tile.getType()));
            gc.fillRect(tile.getX() * TILE_SIZE, tile.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            
            // Якщо потрібна обводка для кожного тайлу:
            gc.setStroke(Color.DARKGRAY);
            gc.strokeRect(tile.getX() * TILE_SIZE, tile.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // 4. Малюємо гравця (якщо він є)
        if (player != null) {
            gc.setFill(Color.RED);
            gc.setStroke(Color.DARKRED);
            gc.setLineWidth(1);
            
            // Рахуємо координати для кола (трохи менше за розмір тайлу)
            double radius = TILE_SIZE / 2.5;
            double offset = (TILE_SIZE - radius * 2) / 2; // центрування
            double px = player.getX() * TILE_SIZE + offset;
            double py = player.getY() * TILE_SIZE + offset;

            gc.fillOval(px, py, radius * 2, radius * 2);
            gc.strokeOval(px, py, radius * 2, radius * 2);
        }
    }

    private Color getTileColor(String type) {
        return switch (type) {
            case "Forest" -> Color.web("#4CAF50");
            case "Water" -> Color.web("#2196F3");
            case "Sand" -> Color.web("#FFC107");
            case "Mountain" -> Color.web("#795548");
            default -> Color.LIGHTGRAY;
        };
    }
}