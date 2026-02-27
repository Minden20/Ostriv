package org.example.ostriv.ostriv.map;

public class Tile {
    private final int x;
    private final int y;
    private final String type;

    public Tile(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Tile{x=" + x + ", y=" + y + ", type='" + type + "'}";
    }
}
