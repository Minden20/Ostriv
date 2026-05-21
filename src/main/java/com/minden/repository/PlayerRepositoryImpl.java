package com.minden.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.minden.entity.ConnectionPool;
import com.minden.entity.Player;
import com.minden.entity.PlayerEventHistory;

public class PlayerRepositoryImpl implements PlayerRepository {
    private final ConnectionPool connectionPool;

    public PlayerRepositoryImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Optional<Player> findById(Integer id) {
        String sql = "SELECT * FROM player WHERE id = ?";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToPlayer(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Player> findByUsername(String username) {
        String sql = "SELECT * FROM player WHERE username = ?";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToPlayer(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Player> findByEmail(String email) {
        String sql = "SELECT * FROM player WHERE email = ?";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToPlayer(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(Player player) {
        String sql = "INSERT INTO player (username, email, password_hash, x, y, gold, energy, current_day) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, player.getUsername());
            stmt.setString(2, player.getEmail());
            stmt.setString(3, player.getPasswordHash());
            stmt.setObject(4, player.getX());
            stmt.setObject(5, player.getY());
            stmt.setObject(6, player.getGold());
            stmt.setObject(7, player.getEnergy());
            stmt.setObject(8, player.getCurrentDay());
            stmt.executeUpdate();
            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    player.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Player player) {
        String sql = "UPDATE player SET username = ?, email = ?, password_hash = ?, x = ?, y = ?, gold = ?, energy = ?, current_day = ? WHERE id = ?";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player.getUsername());
            stmt.setString(2, player.getEmail());
            stmt.setString(3, player.getPasswordHash());
            stmt.setObject(4, player.getX());
            stmt.setObject(5, player.getY());
            stmt.setObject(6, player.getGold());
            stmt.setObject(7, player.getEnergy());
            stmt.setObject(8, player.getCurrentDay());
            stmt.setObject(9, player.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Player> findAll() {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM player";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                players.add(mapRowToPlayer(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }

    @Override
    public void addEventToHistory(Integer playerId, Integer eventId, Integer day) {
        String sql = "INSERT INTO player_event_history (player_id, event_id, occurred_day) VALUES (?, ?, ?)";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, eventId);
            stmt.setInt(3, day);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PlayerEventHistory> getHistoryByPlayerId(Integer playerId) {
        List<PlayerEventHistory> history = new ArrayList<>();
        String sql = "SELECT * FROM player_event_history WHERE player_id = ? ORDER BY occurred_day DESC";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                PlayerEventHistory record = PlayerEventHistory.builder()
                        .id(rs.getObject("id", Integer.class))
                        .playerId(rs.getObject("player_id", Integer.class))
                        .eventId(rs.getObject("event_id", Integer.class))
                        .occurredDay(rs.getObject("occurred_day", Integer.class))
                        .build();
                history.add(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    private Player mapRowToPlayer(ResultSet rs) throws SQLException {
        return Player.builder()
                .id(rs.getObject("id", Integer.class))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .x(rs.getObject("x", Integer.class))
                .y(rs.getObject("y", Integer.class))
                .gold(rs.getObject("gold", Integer.class))
                .energy(rs.getObject("energy", Integer.class))
                .currentDay(rs.getObject("current_day", Integer.class))
                .build();
    }
}