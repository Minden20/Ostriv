package com.minden.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionLog {
    private Integer id;
    private Integer playerId;
    private String actionType;
    private Integer fromX;
    private Integer fromY;
    private Integer toX;
    private Integer toY;
    private Boolean isValid;
    private LocalDateTime createdAt;
}
