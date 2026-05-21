package com.minden.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
   private Integer id;
    private String username;
    private String email;
    private String passwordHash;
    private Integer x;
    private Integer y;
    private Integer gold;
    private Integer energy;
    private Integer currentDay;
}
