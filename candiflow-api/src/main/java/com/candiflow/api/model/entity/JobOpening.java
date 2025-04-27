package com.candiflow.api.model.entity;

import com.candiflow.api.model.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"recruiter", "openingApplicants"})
@Entity
@Table(name = "job_openings")
@AttributeOverride(name = "id", column = @Column(name = "job_opening_id"))
public class JobOpening extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_user_id", nullable = false)
    private User recruiter;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status = JobStatus.OPEN;

    @OneToMany(mappedBy = "jobOpening", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OpeningApplicant> openingApplicants = new LinkedHashSet<>();
}
