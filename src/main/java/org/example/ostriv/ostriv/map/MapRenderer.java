package org.example.ostriv.ostriv.map;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MapRenderer {

    private static final int TILE_SIZE = 10;

    /**
     * Відмальовує MapModel як GridPane з кольоровими прямокутниками.
     */
    public GridPane render(MapModel mapModel) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);

        // Спочатку заповнюємо фон (порожні клітинки)
        for (int y = 0; y < mapModel.getHeight(); y++) {
            for (int x = 0; x < mapModel.getWidth(); x++) {
                Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                rect.setFill(Color.LIGHTGRAY);
                rect.setStroke(Color.GRAY);
                rect.setStrokeWidth(0.5);
                grid.add(rect, x, y);
            }
        }

        // Потім малюємо тайли з даних
        for (Tile tile : mapModel.getTiles()) {
            Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
            rect.setFill(getTileColor(tile.getType()));
            rect.setStroke(Color.DARKGRAY);
            rect.setStrokeWidth(0.5);
            grid.add(rect, tile.getX(), tile.getY());
        }

        return grid;
    }

    private Color getTileColor(String type) {
        return switch (type) {
            case "grass" -> Color.web("#4CAF50");
            case "water" -> Color.web("#2196F3");
            case "sand" -> Color.web("#FFC107");
            case "mountain" -> Color.web("#795548");
            default -> Color.LIGHTGRAY;
        };
    }
}
