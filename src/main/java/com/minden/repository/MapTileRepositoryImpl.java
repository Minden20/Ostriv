package com.minden.repository;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import com.minden.entity.ConnectionPool;
import com.minden.entity.MapTile;
import com.minden.entity.PlayerMapModification;

public class MapTileRepositoryImpl implements MapTileRepository {
    private final ConnectionPool connectionPool;

    public MapTileRepositoryImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Optional<MapTile> findByCoordinates(Integer x, Integer y) {
        String sql = "SELECT * FROM map_tile WHERE x = ? AND y = ?";
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, x);
            stmt.setObject(2, y);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(MapTile.builder()
                        .x(rs.getInt("x"))
                        .y(rs.getInt("y"))
                        .terrainType(rs.getString("terrain_type"))
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(MapTile tile) {
        String sql = "INSERT INTO map_tile (x, y, terrain_type) VALUES (?, ?, ?)";
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, tile.getX());
            stmt.setObject(2, tile.getY());
            stmt.setString(3, tile.getTerrainType());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<MapTile> findAll() {
        List<MapTile> tiles = new ArrayList<>();
        String sql = "SELECT * FROM map_tile";
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                tiles.add(MapTile.builder()
                        .x(rs.getInt("x"))
                        .y(rs.getInt("y"))
                        .terrainType(rs.getString("terrain_type"))
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tiles;
    }

    @Override
    public void delete(Integer x, Integer y) {
        String sql = "DELETE FROM map_tile WHERE x = ? AND y = ?";
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, x);
            stmt.setObject(2, y);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasTiles() {
        String sql = "SELECT COUNT(*) FROM map_tile";
        try (var conn = connectionPool.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void batchInsert(List<MapTile> tiles) {
        String sql = "INSERT INTO map_tile (x, y, terrain_type) VALUES (?, ?, ?)";
        try (var conn = connectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try (var pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < tiles.size(); i++) {
                    MapTile tile = tiles.get(i);
                    pstmt.setInt(1, tile.getX());
                    pstmt.setInt(2, tile.getY());
                    pstmt.setString(3, tile.getTerrainType());
                    pstmt.addBatch();

                    if (i % 1000 == 0 || i == tiles.size() - 1) {
                        pstmt.executeBatch();
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Помилка збереження карти в БД", e);
        }
    }

    @Override
    public void saveModification(Integer playerId, Integer x, Integer y, String newTerrainType) {
        String sql = "MERGE INTO player_map_modification (player_id, x, y, new_terrain_type) KEY (player_id, x, y) VALUES (?, ?, ?, ?)";
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            stmt.setInt(2, x);
            stmt.setInt(3, y);
            stmt.setString(4, newTerrainType);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PlayerMapModification> findModificationsByPlayerId(Integer playerId) {
        List<PlayerMapModification> modifications = new ArrayList<>();
        String sql = "SELECT * FROM player_map_modification WHERE player_id = ?";
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    modifications.add(PlayerMapModification.builder()
                            .playerId(rs.getInt("player_id"))
                            .x(rs.getInt("x"))
                            .y(rs.getInt("y"))
                            .newTerrainType(rs.getString("new_terrain_type"))
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modifications;
    }

    @Override
    public void initializeCityTiles() {
        String sql = "UPDATE map_tile SET terrain_type = 'City' WHERE x BETWEEN 4 AND 8 AND y BETWEEN 4 AND 8";
        try (var connection = connectionPool.getConnection();
             var stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}