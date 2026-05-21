package com.minden.service;

import com.minden.dto.LoginRequest;
import com.minden.dto.PlayerDto;
import com.minden.dto.RegisterRequest;
import com.minden.entity.Player;
import com.minden.exception.AuthException;
import com.minden.exception.UserAlreadyExistsException;
import com.minden.exception.ValidationException;
import com.minden.infrastructure.email.EmailService;
import com.minden.infrastructure.hashing.HashingService;
import com.minden.repository.PlayerRepository;
import com.minden.util.Validator;

/**
 * Реалізація сервісу аутентифікації.
 * Відповідає за реєстрацію та логін гравців з використанням інфраструктурних сервісів.
 */
public class AuthServiceImpl implements AuthenticationService {

    private final PlayerRepository playerRepository;
    private final HashingService hashingService;
    private final EmailService emailService;

    /**
     * Конструктор з ін'єкцією залежності (Dependency Injection).
     *
     * @param playerRepository репозиторій для доступу до даних гравців
     * @param hashingService   інфраструктурний сервіс для роботи з хешуванням
     * @param emailService     інфраструктурний сервіс для роботи з поштою
     */
    public AuthServiceImpl(PlayerRepository playerRepository, HashingService hashingService, EmailService emailService) {
        this.playerRepository = playerRepository;
        this.hashingService = hashingService;
        this.emailService = emailService;
    }

    @Override
    public PlayerDto register(RegisterRequest request)
            throws ValidationException, UserAlreadyExistsException {

        Validator.validateUsername(request.getUsername());
        Validator.validateEmail(request.getEmail());
        Validator.validatePassword(request.getPassword());

        if (playerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "Користувач з ім'ям '" + request.getUsername() + "' вже існує.");
        }

        if (playerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "Користувач з email '" + request.getEmail() + "' вже існує.");
        }

        // Перевірка існування email через інфраструктурний сервіс пошти
        if (!emailService.verifyEmailExists(request.getEmail())) {
            throw new ValidationException("Вказана адреса електронної пошти є недійсною.");
        }

        // Хешування пароля через інфраструктурний сервіс
        String hashedPassword = hashingService.hash(request.getPassword());

        Player player = Player.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .x(0)
                .y(0)
                .gold(100)
                .energy(50)
                .currentDay(1)
                .build();

        playerRepository.save(player);

        // Надсилання листа підтвердження на пошту
        try {
            emailService.sendVerificationEmail(request.getEmail(), request.getUsername());
        } catch (Exception e) {
            System.err.println("Помилка надсилання листа підтвердження: " + e.getMessage());
        }

        return toDto(player);
    }

    @Override
    public PlayerDto login(LoginRequest request) throws AuthException, ValidationException {

        Validator.validateUsername(request.getUsername());
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Пароль не може бути порожнім.");
        }

        Player player = playerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException("Невірний логін або пароль."));

        // Перевірка відповідності хешу пароля через інфраструктурний сервіс
        if (!hashingService.verify(request.getPassword(), player.getPasswordHash())) {
            throw new AuthException("Невірний логін або пароль.");
        }

        return toDto(player);
    }

    /**
     * Конвертує Entity в DTO (без passwordHash).
     */
    private PlayerDto toDto(Player player) {
        return PlayerDto.builder()
                .id(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .x(player.getX())
                .y(player.getY())
                .gold(player.getGold())
                .energy(player.getEnergy())
                .currentDay(player.getCurrentDay())
                .build();
    }
}
