package org.example.ostriv.ostriv;

import org.example.ostriv.ostriv.map.MapDAO;
import org.example.ostriv.ostriv.map.MapModel;
import org.example.ostriv.ostriv.map.MapRenderer;
import org.example.ostriv.ostriv.mobs.Player;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas; // ДОДАНО ІМПОРТ CANVAS
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
// імпорт GridPane більше не потрібен

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
    
    // ЗАМІНИЛИ GridPane на Canvas
    private Canvas mapCanvas; 

    @FXML
    @SuppressWarnings("CallToPrintStackTrace")
    protected void onLoadMapClick() {
        MapDAO mapDAO = new MapDAO();
        renderer = new MapRenderer();
        try {
            mapModel = mapDAO.loadMap();

            // 1. Створюємо полотно Canvas замість GridPane
            mapCanvas = renderer.createMapCanvas(mapModel);

            // 2. Створюємо гравця
            player = new Player(10, 0, 100, 0, 100, 1, "Hero", 10, 10); // damage, exp, hp, inventory, energy, lvl, name, x, y

            // 3. Малюємо всю карту та гравця на Canvas
            renderer.renderAll(mapModel, player);

            // 4. Очищаємо контейнер (на випадок повторного завантаження) і додаємо Canvas
            mapContainer.getChildren().clear();
            mapContainer.getChildren().add(mapCanvas);

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

    private void cameraFollowPlayer() {
        if (mapCanvas == null) return; // Захист від NullPointerException

        double playerPixelX = player.getX() * 50; // 50 = TILE_SIZE
        double playerPixelY = player.getY() * 50;

        double viewWidth = mapContainer.getWidth();
        double viewHeight = mapContainer.getHeight();

        // Тепер рухаємо Canvas, а не GridPane
        mapCanvas.setTranslateX(-(playerPixelX - viewWidth / 2));
        mapCanvas.setTranslateY(-(playerPixelY - viewHeight / 2));
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
            
            // ЗАМІСТЬ видалення/додавання елементів - просто перемальовуємо весь Canvas
            renderer.renderAll(mapModel, player);
            
            cameraFollowPlayer();
            welcomeText.setText("Гравець: (" + player.getX() + ", " + player.getY() + ")");
        }
    }

    public Button getLoadButton() {
        return loadButton;
    }
}