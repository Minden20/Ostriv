package com.minden.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapTile {

    private Integer x;
    private Integer y;
    private String terrainType;

    public int getMovementCost() {
        switch (terrainType.toLowerCase()) {
            case "forest":
                return 1;
            case "water":
                return 999;
            case "mountain":
                return 5;
            case "sand":
                return 2;
            default:
                return 1;
        }
    }
}
