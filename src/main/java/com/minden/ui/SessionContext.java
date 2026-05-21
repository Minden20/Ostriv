package com.minden.ui;

import com.minden.dto.PlayerDto;

/**
 * Глобальний контекст сесії для збереження стану авторизованого користувача.
 * Використовує патерн Singleton.
 */
public class SessionContext {
    private static SessionContext instance;
    private PlayerDto currentUser;

    private SessionContext() {}

    public static synchronized SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public PlayerDto getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(PlayerDto currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }
}
