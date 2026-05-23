package com.minden.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.minden.dto.TreasureDto;
import com.minden.entity.Treasure;
import com.minden.repository.TreasureRepository;

public class TreasureServiceImpl implements TreasureService {
    private final TreasureRepository treasureRepository;

    public TreasureServiceImpl(TreasureRepository treasureRepository) {
        this.treasureRepository = treasureRepository;
    }

    @Override
    public List<TreasureDto> findAll() {
        return treasureRepository.findAll().stream()
                .map(t -> new TreasureDto(
                        t.getId(),
                        t.getX(),
                        t.getY(),
                        t.getMinGold(),
                        t.getMaxGold(),
                        false))
                .collect(Collectors.toList());
    }


    @Override
    public List<TreasureDto> findAllForPlayer(Integer playerId) {
        if (playerId == null) {
            return findAll();
        }
        List<Integer> collectedIds = treasureRepository.findCollectedTreasureIdsByPlayerId(playerId);
        return treasureRepository.findAll().stream()
                .map(t -> new TreasureDto(
                        t.getId(),
                        t.getX(),
                        t.getY(),
                        t.getMinGold(),
                        t.getMaxGold(),
                        collectedIds.contains(t.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public void collectTreasure(Integer playerId, Integer treasureId) {
        if (playerId != null && treasureId != null) {
            if (!treasureRepository.isCollectedByPlayer(playerId, treasureId)) {
                treasureRepository.collectTreasure(playerId, treasureId);
            }
        }
    }

    @Override
    public boolean checkVictoryCondition(Integer playerId) {
        if (playerId == null) {
            return false;
        }
        int totalTreasures = treasureRepository.findAll().size();
        int collectedTreasures = treasureRepository.findCollectedTreasureIdsByPlayerId(playerId).size();
        return collectedTreasures >= totalTreasures && totalTreasures > 0;
    }
}
