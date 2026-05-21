package com.minden.ui.controller;

import com.minden.config.ServiceFactory;
import com.minden.dto.LoginRequest;
import com.minden.dto.PlayerDto;
import com.minden.dto.RegisterRequest;
import com.minden.service.AuthenticationService;
import com.minden.ui.JavaFxApp;
import com.minden.ui.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private VBox emailContainer;
    @FXML private Label titleLabel;
    @FXML private Button actionButton;
    @FXML private Button toggleModeButton;
    @FXML private Label errorLabel;

    private boolean isLoginMode = true;
    private AuthenticationService authService;

    @FXML
    public void initialize() {
        try {
            // Отримуємо сервіс через нашу фабрику
            authService = ServiceFactory.getInstance().getAuthService();
            setupLoginMode();
        } catch (Exception e) {
            showError("Помилка ініціалізації: " + e.getMessage());
        }
    }

    @FXML
    private void handleAction(ActionEvent event) {
        errorLabel.setVisible(false);
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            if (isLoginMode) {
                // Виклик логіну
                LoginRequest req = new LoginRequest(username, password);
                PlayerDto player = authService.login(req);
                onSuccess(player);
            } else {
                // Виклик реєстрації
                String email = emailField.getText();
                RegisterRequest req = new RegisterRequest(username, email, password);
                PlayerDto player = authService.register(req);
                onSuccess(player);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void toggleMode(ActionEvent event) {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            setupLoginMode();
        } else {
            setupRegisterMode();
        }
    }

    private void setupLoginMode() {
        titleLabel.setText("Вхід у систему");
        emailContainer.setVisible(false);
        emailContainer.setManaged(false);
        actionButton.setText("Увійти");
        toggleModeButton.setText("Немає акаунту? Зареєструватись");
        errorLabel.setVisible(false);
    }

    private void setupRegisterMode() {
        titleLabel.setText("Реєстрація");
        emailContainer.setVisible(true);
        emailContainer.setManaged(true);
        actionButton.setText("Зареєструватись");
        toggleModeButton.setText("Вже є акаунт? Увійти");
        errorLabel.setVisible(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void onSuccess(PlayerDto player) {
        // Зберігаємо юзера в глобальну сесію
        SessionContext.getInstance().setCurrentUser(player);
        // Переходимо на головний екран
        JavaFxApp.setRoot("main");
    }
}
