package com.minden.service;

import com.minden.entity.MapTile;
import java.util.List;

public interface MapService {
    void importMapFromJson(String filePath);
    List<MapTile> getAllTiles();
    List<MapTile> getMapForPlayer(Integer playerId);
    void modifyMapTile(Integer playerId, Integer x, Integer y, String newTerrainType);
}
