package com.minden.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreasureDto {
    private Integer id;
    private Integer x;
    private Integer y;
    private Integer minGold;
    private Integer maxGold;
    private Boolean isCollected;
}
