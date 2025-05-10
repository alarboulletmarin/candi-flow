package com.candiflow.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le nombre de candidats par source
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceCount {
    private String sourceName;
    private long count;
}
