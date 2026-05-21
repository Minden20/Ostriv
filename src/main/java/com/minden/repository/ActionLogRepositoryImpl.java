package com.minden.repository;

import com.minden.entity.ActionLog;
import com.minden.entity.ConnectionPool;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ActionLogRepositoryImpl implements ActionLogRepository {
    private final ConnectionPool connectionPool;

    public ActionLogRepositoryImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void save(ActionLog log) {
        String sql = "INSERT INTO action_log (player_id, action_type, from_x, from_y, to_x, to_y, is_valid, created_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, log.getPlayerId());
            stmt.setString(2, log.getActionType());
            stmt.setObject(3, log.getFromX());
            stmt.setObject(4, log.getFromY());
            stmt.setObject(5, log.getToX());
            stmt.setObject(6, log.getToY());
            stmt.setObject(7, log.getIsValid());
            stmt.setObject(8, log.getCreatedAt()); // JDBC 4.2 підтримує LocalDateTime

            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ActionLog> findByPlayerId(Integer playerId) {
        List<ActionLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM action_log WHERE player_id = ? ORDER BY created_at DESC";

        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {

            stmt.setObject(1, playerId);
            var rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapRowToActionLog(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    @Override
    public List<ActionLog> findAll() {
        List<ActionLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM action_log ORDER BY created_at DESC";

        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {

            var rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapRowToActionLog(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    private ActionLog mapRowToActionLog(ResultSet rs) throws SQLException {
        return ActionLog.builder()
                .id(rs.getInt("id"))
                .playerId(rs.getInt("player_id"))
                .actionType(rs.getString("action_type"))
                .fromX(rs.getObject("from_x", Integer.class))
                .fromY(rs.getObject("from_y", Integer.class))
                .toX(rs.getObject("to_x", Integer.class))
                .toY(rs.getObject("to_y", Integer.class))
                .isValid(rs.getBoolean("is_valid"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
