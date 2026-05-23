package com.minden.ui.controller;

import com.minden.dto.PlayerDto;
import com.minden.ui.JavaFxApp;
import com.minden.ui.SessionContext;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private Label userGreetingLabel;
    @FXML private StackPane contentArea;
    @FXML private Label goldLabel;
    @FXML private Label energyLabel;

    private Node mapView;
    private StackPane activeOverlay;

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;

        // Встановлюємо привітання та статистику
        updatePlayerStats();

        // Завантажуємо ігрову карту за замовчуванням
        loadMapView();
    }

    public void updatePlayerStats() {
        PlayerDto currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (goldLabel != null) {
                goldLabel.setText(String.valueOf(currentUser.getGold() != null ? currentUser.getGold() : 0));
            }
            if (energyLabel != null) {
                energyLabel.setText(String.valueOf(currentUser.getEnergy() != null ? currentUser.getEnergy() : 0));
            }
            if (userGreetingLabel != null) {
                userGreetingLabel.setText("Привіт, " + currentUser.getUsername() + "!");
            }
        }
    }

    private void loadMapView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/views/map.fxml"));
            mapView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(mapView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не вдалося завантажити карту: /fxml/views/map.fxml");
        }
    }

    @FXML
    private void handleShowLogs(ActionEvent event) {
        if (activeOverlay != null) {
            closeLogsOverlay();
        } else {
            showLogsOverlay();
        }
    }

    private void showLogsOverlay() {
        try {
            // Завантажуємо вигляд журналу дій
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/views/logs.fxml"));
            Node logsView = loader.load();

            // Створюємо контейнер для затемнення фону
            StackPane overlayContainer = new StackPane();
            overlayContainer.getStyleClass().add("overlay-container");

            // Створюємо модальне вікно
            VBox gameModal = new VBox(15);
            gameModal.getStyleClass().add("game-modal");
            gameModal.setMaxSize(850, 550);
            gameModal.setPrefSize(850, 550);

            // Шапка модального вікна
            HBox modalHeader = new HBox();
            modalHeader.getStyleClass().add("modal-header");
            modalHeader.setAlignment(Pos.CENTER_LEFT);

            Label titleLabel = new Label("Щоденник подорожей");
            titleLabel.getStyleClass().add("modal-title");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button closeButton = new Button("Закрити");
            closeButton.getStyleClass().add("primary-button");
            closeButton.setOnAction(e -> closeLogsOverlay());

            modalHeader.getChildren().addAll(titleLabel, spacer, closeButton);

            // Додаємо шапку та таблицю логів в модальне вікно
            VBox.setVgrow(logsView, Priority.ALWAYS);
            gameModal.getChildren().addAll(modalHeader, logsView);

            // Розміщуємо модальне вікно в центрі оверлею
            overlayContainer.getChildren().add(gameModal);
            StackPane.setAlignment(gameModal, Pos.CENTER);

            // Клік по затемненому фону закриває вікно
            overlayContainer.setOnMouseClicked(e -> {
                if (e.getTarget() == overlayContainer) {
                    closeLogsOverlay();
                }
            });

            // Додаємо оверлей поверх карти в головний StackPane
            activeOverlay = overlayContainer;
            contentArea.getChildren().add(overlayContainer);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не вдалося завантажити оверлей журналу дій!");
        }
    }

    private void closeLogsOverlay() {
        if (activeOverlay != null) {
            contentArea.getChildren().remove(activeOverlay);
            activeOverlay = null;
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionContext.getInstance().logout();
        JavaFxApp.setRoot("login");
    }
}
