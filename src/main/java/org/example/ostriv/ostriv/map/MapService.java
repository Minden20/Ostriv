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

        // keys in the JSON may be capitalized; try both variants
        Object widthObj = mapData.get("width");
        if (widthObj == null) widthObj = mapData.get("Width");
        Object heightObj = mapData.get("height");
        if (heightObj == null) heightObj = mapData.get("Height");

        int width = ((Long) widthObj).intValue();
        int height = ((Long) heightObj).intValue();

        Object tilesObj = mapData.get("tiles");
        if (tilesObj == null) tilesObj = mapData.get("Tiles");
        List<Tile> tiles = new ArrayList<>();

        if (tilesObj instanceof List<?> tileList) {
            for (Object item : tileList) {
                if (item instanceof JsonObject tileJson) {
                    Object xObj = tileJson.get("x");
                    if (xObj == null) xObj = tileJson.get("X");
                    Object yObj = tileJson.get("y");
                    if (yObj == null) yObj = tileJson.get("Y");
                    String type = (String) tileJson.get("type");
                    if (type == null) type = (String) tileJson.get("Type");
                    int x = ((Long) xObj).intValue();
                    int y = ((Long) yObj).intValue();
                    tiles.add(new Tile(x, y, type));
                }
            }
        }

        return new MapModel(width, height, tiles);
    }
}
