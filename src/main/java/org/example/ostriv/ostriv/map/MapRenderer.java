package org.example.ostriv.ostriv.map;

import org.example.ostriv.ostriv.player.Player;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class MapRenderer {

    private static final int TILE_SIZE = 50;

    private GridPane grid;

    /**
     * Відмальовує MapModel як GridPane з кольоровими прямокутниками.
     */
    public GridPane render(MapModel mapModel) {
        grid = new GridPane();
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

    /**
     * Додає маркер гравця на карту (червоне коло поверх тайлу).
     */
    public void renderPlayer(Player player) {
        if (grid == null) return;

        StackPane playerCell = new StackPane();
        Rectangle bg = new Rectangle(TILE_SIZE, TILE_SIZE);
        bg.setFill(Color.TRANSPARENT);

        Circle playerMarker = new Circle(TILE_SIZE / 2.5);
        playerMarker.setFill(Color.RED);
        playerMarker.setStroke(Color.DARKRED);
        playerMarker.setStrokeWidth(1);

        playerCell.getChildren().addAll(bg, playerMarker);
        grid.add(playerCell, player.getX(), player.getY());
    }

    /**
     * Оновлює позицію гравця — видаляє старий маркер і малює новий.
     */
    public void updatePlayerPosition(Player player) {
        // Видаляємо попередній маркер гравця (StackPane)
        grid.getChildren().removeIf(node -> node instanceof StackPane);
        renderPlayer(player);
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
