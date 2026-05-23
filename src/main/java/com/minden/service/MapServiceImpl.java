package com.minden.service;

import com.minden.entity.MapTile;
import com.minden.entity.PlayerMapModification;
import com.minden.repository.MapTileRepository;
import com.minden.util.JsonFileHandler;
import com.minden.util.SimpleJsonParser;
import com.minden.util.SimpleJsonParser.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MapServiceImpl implements MapService {

    private final MapTileRepository mapRepository;
    private final JsonFileHandler fileHandler;

    public MapServiceImpl(MapTileRepository mapRepository) {
        this.mapRepository = mapRepository;
        this.fileHandler = new JsonFileHandler();
    }

    @Override
    public void importMapFromJson(String filePath) {
        if (mapRepository.hasTiles()) {
            System.out.println("Карта вже імпортована в базу даних. Пропускаємо імпорт.");
            return;
        }

        System.out.println("Починаємо імпорт карти з файлу: " + filePath);
        try {
            if (!fileHandler.fileExists(filePath)) {
                System.err.println("Файл карти не знайдено: " + filePath);
                return;
            }

            String jsonString = fileHandler.readFile(filePath);
            JsonObject rootNode = SimpleJsonParser.parseObject(jsonString);
            
            if (rootNode != null && rootNode.containsKey("map")) {
                Object mapObj = rootNode.get("map");
                
                // В нашому парсері вкладені об'єкти повертаються як рядки
                JsonObject innerMap = null;
                if (mapObj instanceof String) {
                    innerMap = SimpleJsonParser.parseObject((String) mapObj);
                } else if (mapObj instanceof JsonObject) {
                    innerMap = (JsonObject) mapObj;
                }
                
                if (innerMap != null && innerMap.containsKey("Tiles")) {
                    Object tilesObj = innerMap.get("Tiles");
                    parseAndInsertTiles(tilesObj);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Помилка імпорту карти: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void parseAndInsertTiles(Object tilesObj) {
        List<JsonObject> tilesList = null;
        
        // В парсері масиви теж повертаються як рядки
        if (tilesObj instanceof String) {
            tilesList = SimpleJsonParser.parseArray((String) tilesObj);
        } else if (tilesObj instanceof List) {
            // На випадок якщо парсер колись оновлять
            tilesList = (List<JsonObject>) tilesObj;
        }

        if (tilesList != null) {
            List<MapTile> mapTiles = new ArrayList<>();
            
            for (Object tileItem : tilesList) {
                JsonObject tileObj = null;
                if (tileItem instanceof String) {
                    tileObj = SimpleJsonParser.parseObject((String) tileItem);
                } else if (tileItem instanceof JsonObject) {
                    tileObj = (JsonObject) tileItem;
                }
                
                if (tileObj != null) {
                    int x = parseInteger(tileObj.get("X"));
                    int y = parseInteger(tileObj.get("Y"));
                    String type = (String) tileObj.get("Type");
                    
                    mapTiles.add(MapTile.builder().x(x).y(y).terrainType(type).build());
                }
            }
            
            System.out.println("Розпарсено " + mapTiles.size() + " клітинок. Зберігаємо в БД...");
            mapRepository.batchInsert(mapTiles);
            mapRepository.initializeCityTiles();
            System.out.println("Карту успішно збережено в БД та ініціалізовано місто!");
        }
    }
    
    private int parseInteger(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        }
        return 0;
    }

    @Override
    public List<MapTile> getAllTiles() {
        return mapRepository.findAll();
    }

    @Override
    public List<MapTile> getMapForPlayer(Integer playerId) {
        List<MapTile> baseTiles = getAllTiles();
        if (playerId == null) {
            return baseTiles;
        }
        
        List<PlayerMapModification> modifications = mapRepository.findModificationsByPlayerId(playerId);
        if (modifications.isEmpty()) {
            return baseTiles;
        }

        java.util.Map<String, String> modMap = new java.util.HashMap<>();
        for (PlayerMapModification mod : modifications) {
            modMap.put(mod.getX() + "," + mod.getY(), mod.getNewTerrainType());
        }

        for (MapTile tile : baseTiles) {
            String key = tile.getX() + "," + tile.getY();
            if (modMap.containsKey(key)) {
                tile.setTerrainType(modMap.get(key));
            }
        }
        return baseTiles;
    }

    @Override
    public void modifyMapTile(Integer playerId, Integer x, Integer y, String newTerrainType) {
        if (playerId != null && x != null && y != null && newTerrainType != null) {
            mapRepository.saveModification(playerId, x, y, newTerrainType);
        }
    }
}
