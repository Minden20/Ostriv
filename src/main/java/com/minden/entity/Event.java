package com.minden.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Integer id;
    private String name;
    private String description;
    private Integer minGoldPenalty;
    private Integer maxGoldPenalty;
}
