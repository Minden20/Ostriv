package org.example.ostriv.ostriv.map;

import java.util.ArrayList;
import java.util.List;

import org.example.ostriv.ostriv.utill.SimpleJsonParser;
import org.example.ostriv.ostriv.utill.SimpleJsonParser.JsonObject;

public class MapService {

    /**
     * Парсить JSON-рядок з картою у MapModel.
     * Очікує формат: {"map": {"width": N, "height": N, "tiles": [...]}}
     */
    public MapModel parse(String json) {
        JsonObject root = SimpleJsonParser.parseObject(json);
        if (root == null) {
            throw new IllegalArgumentException("Невалідний JSON");
        }

        Object mapObj = root.get("map");
        if (!(mapObj instanceof JsonObject mapData)) {
            throw new IllegalArgumentException("Відсутній об'єкт 'map'");
        }

        int width = ((Long) mapData.get("width")).intValue();
        int height = ((Long) mapData.get("height")).intValue();

        Object tilesObj = mapData.get("tiles");
        List<Tile> tiles = new ArrayList<>();

        if (tilesObj instanceof List<?> tileList) {
            for (Object item : tileList) {
                if (item instanceof JsonObject tileJson) {
                    int x = ((Long) tileJson.get("x")).intValue();
                    int y = ((Long) tileJson.get("y")).intValue();
                    String type = (String) tileJson.get("type");
                    tiles.add(new Tile(x, y, type));
                }
            }
        }

        return new MapModel(width, height, tiles);
    }
}
