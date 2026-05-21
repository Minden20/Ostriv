package com.minden.repository;

import java.util.List;
import java.util.Optional;

import com.minden.entity.Player;
import com.minden.entity.PlayerEventHistory;

public interface PlayerRepository {
    Optional<Player> findById(Integer id);

    Optional<Player> findByUsername(String username);

    Optional<Player> findByEmail(String email);

    void save(Player player);

    List<Player> findAll();

    void update(Player player);


    void addEventToHistory(Integer playerId, Integer eventId, Integer day);

    List<PlayerEventHistory> getHistoryByPlayerId(Integer playerId);
}
