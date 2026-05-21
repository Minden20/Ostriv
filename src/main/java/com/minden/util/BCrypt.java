package com.minden.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Утиліта для хешування паролів на основі PBKDF2WithHmacSHA256.
 * Адаптовано з проекту infosyssecurityagency.
 */
public class BCrypt {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Хешує пароль із заданою сіллю.
     *
     * @param password пароль у відкритому вигляді
     * @param salt     сіль у форматі Base64
     * @return хеш у форматі "salt:hash"
     */
    public static String hashpw(String password, String salt) {
        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = Base64.getDecoder().decode(salt);

        byte[] hashedBytes = hash(passwordChars, saltBytes);
        return salt + ":" + Base64.getEncoder().encodeToString(hashedBytes);
    }

    /**
     * Перевіряє, чи відповідає пароль збереженому хешу.
     *
     * @param password пароль у відкритому вигляді
     * @param hashed   збережений хеш у форматі "salt:hash"
     * @return true, якщо пароль вірний
     */
    public static boolean checkpw(String password, String hashed) {
        try {
            String[] parts = hashed.split(":");

            if (parts.length < 2) return false;

            String saltStr = parts[0];
            String newHashForInput = hashpw(password, saltStr);
            return newHashForInput.equals(hashed);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Генерує випадкову сіль у форматі Base64.
     *
     * @return сіль у форматі Base64
     */
    public static String gensalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing password", e);
        } finally {
            spec.clearPassword();
        }
    }
}
