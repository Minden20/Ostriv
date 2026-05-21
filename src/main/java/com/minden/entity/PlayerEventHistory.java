package com.minden.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEventHistory {
    private Integer id;
    private Integer playerId;
    private Integer eventId;
    private Integer occurredDay;
}
