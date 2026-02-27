package org.example.ostriv.ostriv.map;

import java.util.List;

public class MapModel {
    private final int width;
    private final int height;
    private final List<Tile> tiles;

    public MapModel(int width, int height, List<Tile> tiles) {
        this.width = width;
        this.height = height;
        this.tiles = tiles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    @Override
    public String toString() {
        return "MapModel{width=" + width + ", height=" + height + ", tiles=" + tiles.size() + "}";
    }
}
