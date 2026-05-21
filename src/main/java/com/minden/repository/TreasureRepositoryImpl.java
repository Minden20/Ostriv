package com.minden.repository;

import com.minden.entity.ConnectionPool;
import com.minden.entity.Treasure;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreasureRepositoryImpl implements TreasureRepository {
    private final ConnectionPool connectionPool;

    public TreasureRepositoryImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }


    @Override
    public List<Treasure> findByCoordinates(Integer x, Integer y) {
        List<Treasure> treasures = new ArrayList<>();
        String sql = "SELECT * FROM treasure_template WHERE x = ? AND y = ?";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, x);
            stmt.setObject(2, y);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                treasures.add(mapRowToTreasure(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return treasures;
    }

    private Treasure mapRowToTreasure(java.sql.ResultSet rs) throws java.sql.SQLException {
        return Treasure.builder()
                .id(rs.getInt("id"))
                .x(rs.getInt("x"))
                .y(rs.getInt("y"))
                .minGold(rs.getInt("min_gold"))
                .maxGold(rs.getInt("max_gold"))
                .build();
    }

    @Override
    public Optional<Treasure> findById(Integer id) {
        String sql = "SELECT * FROM treasure_template WHERE id = ?";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToTreasure(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Treasure> findAll() {
        List<Treasure> treasures = new ArrayList<>();
        String sql = "SELECT * FROM treasure_template";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                treasures.add(mapRowToTreasure(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return treasures;
    }

    @Override
    public void collectTreasure(Integer playerId, Integer treasureId) {
        String sql = "INSERT INTO player_collected_treasure (player_id, treasure_template_id) VALUES (?, ?)";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, treasureId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isCollectedByPlayer(Integer playerId, Integer treasureId) {
        String sql = "SELECT COUNT(*) FROM player_collected_treasure WHERE player_id = ? AND treasure_template_id = ?";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, treasureId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Integer> findCollectedTreasureIdsByPlayerId(Integer playerId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT treasure_template_id FROM player_collected_treasure WHERE player_id = ?";
        try (var connection = connectionPool.getConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("treasure_template_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }
}
