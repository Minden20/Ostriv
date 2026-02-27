package org.example.ostriv.ostriv;

import org.example.ostriv.ostriv.map.MapDAO;
import org.example.ostriv.ostriv.map.MapModel;
import org.example.ostriv.ostriv.map.MapRenderer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

public class HelloController {

    @FXML
    private ScrollPane mapContainer;

    @FXML
    private Label welcomeText;

    @FXML
    private Button loadButton;

    @FXML
    protected void onLoadMapClick() {
        MapDAO mapDAO = new MapDAO();
        MapRenderer renderer = new MapRenderer();
        try {
            MapModel mapModel = mapDAO.loadMap();
            GridPane mapGrid = renderer.render(mapModel);
            mapContainer.setContent(mapGrid);
            welcomeText.setText("Карту завантажено: " + mapModel.getWidth() + "x" + mapModel.getHeight());
        } catch (Exception e) {
            welcomeText.setText("Помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
