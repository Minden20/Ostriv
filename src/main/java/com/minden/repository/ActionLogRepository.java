package com.minden.repository;

import java.util.List;

import com.minden.entity.ActionLog;

public interface ActionLogRepository {
    void save(ActionLog log);

    List<ActionLog> findByPlayerId(Integer playerId);

    List<ActionLog> findAll();
}
