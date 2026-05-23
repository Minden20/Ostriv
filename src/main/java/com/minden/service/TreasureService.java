package com.minden.service;

import com.minden.dto.TreasureDto;
import java.util.List;
import java.util.Optional;

public interface TreasureService {
    List<TreasureDto> findAll();
    List<TreasureDto> findAllForPlayer(Integer playerId);
    void collectTreasure(Integer playerId, Integer treasureId);
    boolean checkVictoryCondition(Integer playerId);
}
