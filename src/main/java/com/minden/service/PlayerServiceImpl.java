package com.minden.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.minden.dto.PlayerDto;
import com.minden.entity.Player;
import com.minden.repository.PlayerRepository;

/**
 * Реалізація сервісу бізнес-операцій над гравцями. Інкапсулює бізнес-логіку та
 * повертає DTO замість Entity.
 */
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    /**
     * Конструктор з ін'єкцією залежності (Dependency Injection).
     *
     * @param playerRepository репозиторій для доступу до даних гравців
     */
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Optional<PlayerDto> findById(Integer id) {
        return playerRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<PlayerDto> findAll() {
        return playerRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
