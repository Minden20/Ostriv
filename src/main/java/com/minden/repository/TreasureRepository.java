package com.minden.repository;

import java.util.List;
import java.util.Optional;

import com.minden.entity.Treasure;

public interface TreasureRepository {
    Optional<Treasure> findById(Integer id);

    List<Treasure> findAll();

    List<Treasure> findByCoordinates(Integer x, Integer y);

    void collectTreasure(Integer playerId, Integer treasureId);

    boolean isCollectedByPlayer(Integer playerId, Integer treasureId);

    List<Integer> findCollectedTreasureIdsByPlayerId(Integer playerId);
}
