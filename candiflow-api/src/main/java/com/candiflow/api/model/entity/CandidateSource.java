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
@Table(name = "candidate_sources", uniqueConstraints = {
        @UniqueConstraint(name = "uk_candidate_sources_name", columnNames = "name")
})
@AttributeOverride(name = "id", column = @Column(name = "source_id"))
public class CandidateSource extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name; // Ex: 'LinkedIn', 'Site Carrière', 'Cooptation'

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // --- Relations ---

    @OneToMany(mappedBy = "source", fetch = FetchType.LAZY)
    private Set<OpeningApplicant> openingApplicants = new LinkedHashSet<>();
}

