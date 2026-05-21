package com.minden.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Treasure {
    private Integer id;
    private Integer x;
    private Integer y;
    private Integer minGold;
    private Integer maxGold;
}
