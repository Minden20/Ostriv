package com.minden.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Integer id;
    private String name;
    private String description;
    private Integer minGoldPenalty;
    private Integer maxGoldPenalty;
}
