package org.example.ostriv.ostriv.map;

import org.example.ostriv.ostriv.utill.JsonFileHandler;

public class MapDAO {
    private static final String FILE_PATH = "src/main/java/org/example/ostriv/ostriv/map/map.json";

    private final JsonFileHandler jsonFileHandler;
    private final MapService mapService;

    public MapDAO() {
        this.jsonFileHandler = new JsonFileHandler();
        this.mapService = new MapService();
    }

    public MapModel loadMap() throws Exception {
        if (!jsonFileHandler.fileExists(FILE_PATH)) {
            throw new Exception("Файл з картою не знайдено: " + FILE_PATH);
        }
        String json = jsonFileHandler.readFile(FILE_PATH);
        return mapService.parse(json);
    }
}
