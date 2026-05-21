package com.minden.exception;

/**
 * Виняток, що виникає при помилці валідації вхідних даних.
 * Наприклад: невірний формат email, занадто короткий пароль.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
