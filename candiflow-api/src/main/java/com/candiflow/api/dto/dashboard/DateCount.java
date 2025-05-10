package com.candiflow.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour le nombre d'éléments par date
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateCount {
    private LocalDate date;
    private long count;
}
