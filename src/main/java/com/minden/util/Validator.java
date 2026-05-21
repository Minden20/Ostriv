package com.minden.util;

import com.minden.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Утиліта для валідації вхідних даних користувача.
 * Адаптовано з проекту infosyssecurityagency.
 */
public class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    /**
     * Перевіряє коректність email адреси.
     *
     * @param email email для перевірки
     * @throws ValidationException якщо email порожній або невалідний
     */
    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Поле email не може бути порожнім.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Некоректний формат email.");
        }
    }

    /**
     * Перевіряє коректність пароля.
     *
     * @param password пароль для перевірки
     * @throws ValidationException якщо пароль порожній або менше 8 символів
     */
    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Пароль не може бути порожнім.");
        }
        if (password.length() < 8) {
            throw new ValidationException("Пароль повинен містити мінімум 8 символів.");
        }
    }

    /**
     * Перевіряє коректність імені користувача.
     *
     * @param username ім'я для перевірки
     * @throws ValidationException якщо username порожнє або менше 3 символів
     */
    public static void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Ім'я користувача не може бути порожнім.");
        }
        if (username.length() < 3) {
            throw new ValidationException("Ім'я користувача повинно містити мінімум 3 символи.");
        }
    }
}
