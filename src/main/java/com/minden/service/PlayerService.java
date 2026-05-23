package com.minden.service;

import java.util.List;
import java.util.Optional;

import com.minden.dto.PlayerDto;

/**
 * Інтерфейс сервісу для бізнес-операцій над гравцями. Забезпечує CRUD з
 * валідацією та повертає DTO.
 */
public interface PlayerService {

    /**
     * Отримує гравця за ID.
     *
     * @param id ідентифікатор гравця
     * @return DTO гравця або порожній Optional
     */
    Optional<PlayerDto> findById(Integer id);

    /**
     * Отримує список усіх гравців.
     *
     * @return список DTO гравців
     */
    List<PlayerDto> findAll();

}
