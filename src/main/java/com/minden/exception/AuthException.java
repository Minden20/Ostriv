package com.minden.exception;

/**
 * Виняток, що виникає при невдалій спробі аутентифікації.
 * Наприклад: невірний логін або пароль.
 */
public class AuthException extends Exception {

    public AuthException(String message) {
        super(message);
    }
}
