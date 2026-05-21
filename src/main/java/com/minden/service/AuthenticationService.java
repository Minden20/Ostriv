package com.minden.service;

import com.minden.dto.LoginRequest;
import com.minden.dto.PlayerDto;
import com.minden.dto.RegisterRequest;
import com.minden.exception.AuthException;
import com.minden.exception.UserAlreadyExistsException;
import com.minden.exception.ValidationException;

/**
 * Інтерфейс сервісу аутентифікації.
 * Визначає контракт для реєстрації та логіну користувачів.
 */
public interface AuthenticationService {

    /**
     * Реєструє нового гравця у системі.
     *
     * @param request дані для реєстрації (username, email, password)
     * @return DTO зареєстрованого гравця
     * @throws ValidationException        якщо дані не пройшли валідацію
     * @throws UserAlreadyExistsException якщо username або email вже зайняті
     */
    PlayerDto register(RegisterRequest request)
            throws ValidationException, UserAlreadyExistsException;

    /**
     * Аутентифікує гравця за логіном та паролем.
     *
     * @param request дані для логіну (username, password)
     * @return DTO аутентифікованого гравця
     * @throws AuthException       якщо логін або пароль невірні
     * @throws ValidationException якщо дані не пройшли валідацію
     */
    PlayerDto login(LoginRequest request) throws AuthException, ValidationException;
}
