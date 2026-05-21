package com.minden.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Головний клас для запуску JavaFX додатку.
 */
public class JavaFxApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Встановлюємо загальні параметри вікна
        primaryStage.setTitle("RPG Admin Panel");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Завантажуємо карту з JSON при старті (якщо її ще немає в БД)
        try {
            com.minden.config.ServiceFactory.getInstance().getMapService().importMapFromJson("data/map.json");
        } catch (Exception e) {
            System.err.println("Помилка при завантаженні карти: " + e.getMessage());
        }
        
        // Завантажуємо вікно логіну як перше вікно
        setRoot("login");
        primaryStage.show();
    }

    /**
     * Метод для зручного перемикання екранів (сцен).
     * @param fxmlName назва fxml файлу (без розширення .fxml)
     */
    public static void setRoot(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(JavaFxApp.class.getResource("/fxml/" + fxmlName + ".fxml"));
            Parent root = loader.load();
            
            Scene scene;
            if (primaryStage.getScene() == null) {
                scene = new Scene(root, 1024, 768);
                primaryStage.setScene(scene);
            } else {
                scene = primaryStage.getScene();
                scene.setRoot(root);
            }
            
            // Підключаємо глобальний CSS файл стилів
            scene.getStylesheets().clear();
            scene.getStylesheets().add(JavaFxApp.class.getResource("/css/style.css").toExternalForm());
            
        } catch (Exception e) {
            System.err.println("Помилка завантаження екрану: " + fxmlName);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
