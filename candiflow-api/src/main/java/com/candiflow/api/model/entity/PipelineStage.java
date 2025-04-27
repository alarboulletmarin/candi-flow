package com.candiflow.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"openingApplicants"})
@Entity
@Table(name = "pipeline_stages", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pipeline_stages_name", columnNames = "name"),
        @UniqueConstraint(name = "uk_pipeline_stages_display_order", columnNames = "display_order")
})
@AttributeOverride(name = "id", column = @Column(name = "stage_id"))
public class PipelineStage extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false, unique = true)
    private Integer displayOrder;

    @Column(name = "is_end_stage", nullable = false)
    private boolean isEndStage = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // --- Relations ---

    @OneToMany(mappedBy = "currentStage", fetch = FetchType.LAZY)
    private Set<OpeningApplicant> openingApplicants = new LinkedHashSet<>();
}
