package org.example.ostriv.ostriv;

import org.example.ostriv.ostriv.map.MapDAO;
import org.example.ostriv.ostriv.map.MapModel;
import org.example.ostriv.ostriv.map.MapRenderer;
import org.example.ostriv.ostriv.player.Player;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class HelloController {

    @FXML
    private Pane mapContainer;

    @FXML
    private Label welcomeText;

    @FXML
    private Button loadButton;

    private Player player;
    private MapRenderer renderer;
    private MapModel mapModel;

    @FXML
    protected void onLoadMapClick() {
        MapDAO mapDAO = new MapDAO();
        renderer = new MapRenderer();
        try {
            mapModel = mapDAO.loadMap();
            mapGrid = renderer.render(mapModel);

            // Створюємо гравця на позиції (0, 0)
            player = new Player(10, 0, 100, 0, 1, "Hero", 0, 0);
            renderer.renderPlayer(player);

            mapContainer.getChildren().add(mapGrid);
            welcomeText.setText("Карту завантажено: " + mapModel.getWidth() + "x" + mapModel.getHeight()
                    + " | Гравець: (" + player.getX() + ", " + player.getY() + ")");

            // Фокус на контейнер для обробки клавіш
            mapContainer.setFocusTraversable(true);
            mapContainer.requestFocus();
            mapContainer.setOnKeyPressed(this::handleKeyPress);
            cameraFollowPlayer();
        } catch (Exception e) {
            welcomeText.setText("Помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private GridPane mapGrid;

    private void cameraFollowPlayer() {
        double playerPixelX = player.getX() * 50; // 50 = TILE_SIZE
        double playerPixelY = player.getY() * 50;

        double viewWidth = mapContainer.getWidth();
        double viewHeight = mapContainer.getHeight();

        mapGrid.setTranslateX(-(playerPixelX - viewWidth / 2));
        mapGrid.setTranslateY(-(playerPixelY - viewHeight / 2));
    }

    private void handleKeyPress(KeyEvent event) {
        if (player == null || renderer == null || mapModel == null)
            return;

        int newX = player.getX();
        int newY = player.getY();

        switch (event.getCode()) {
            case W, UP -> newY--;
            case S, DOWN -> newY++;
            case A, LEFT -> newX--;
            case D, RIGHT -> newX++;
            default -> {
                return;
            }
        }

        // Перевіряємо межі карти
        if (newX >= 0 && newX < mapModel.getWidth() && newY >= 0 && newY < mapModel.getHeight()) {
            player.setX(newX);
            player.setY(newY);
            renderer.updatePlayerPosition(player);
            cameraFollowPlayer();
            welcomeText.setText("Гравець: (" + player.getX() + ", " + player.getY() + ")");
        }
    }
}
