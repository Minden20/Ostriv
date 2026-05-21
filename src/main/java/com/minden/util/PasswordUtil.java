package com.minden.util;

/**
 * Утиліта-обгортка для хешування та перевірки паролів.
 * Делегує роботу класу {@link BCrypt}.
 */
public class PasswordUtil {

    /**
     * Хешує пароль за допомогою BCrypt (PBKDF2).
     *
     * @param password пароль у відкритому вигляді
     * @return хеш у форматі "salt:hash"
     */
    public static String hashPassword(String password) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }

    /**
     * Перевіряє відповідність пароля збереженому хешу.
     *
     * @param password пароль у відкритому вигляді
     * @param hashed   збережений хеш
     * @return true, якщо пароль відповідає хешу
     */
    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
