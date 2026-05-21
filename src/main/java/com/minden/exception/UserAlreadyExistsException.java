package com.minden.exception;

/**
 * Виняток, що виникає при спробі зареєструвати користувача
 * з вже існуючим username або email.
 */
public class UserAlreadyExistsException extends Exception {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
