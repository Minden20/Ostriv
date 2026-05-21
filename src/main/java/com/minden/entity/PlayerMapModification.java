package com.minden.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMapModification {
    private Integer playerId;
    private Integer x;
    private Integer y;
    private String newTerrainType;
}
