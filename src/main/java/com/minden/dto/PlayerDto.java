package com.minden.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object для передачі даних гравця у шар представлення.
 * Не містить passwordHash для безпеки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {
    private Integer id;
    private String username;
    private String email;
    private Integer x;
    private Integer y;
    private Integer gold;
    private Integer energy;
    private Integer currentDay;
}
