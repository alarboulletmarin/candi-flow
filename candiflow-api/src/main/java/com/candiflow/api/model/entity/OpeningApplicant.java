package com.candiflow.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"jobOpening", "currentStage", "source", "recruiterNotes", "tags"})
@Entity
@Table(name = "opening_applicants")
@AttributeOverride(name = "id", column = @Column(name = "applicant_id"))
public class OpeningApplicant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_opening_id", nullable = false)
    private JobOpening jobOpening;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "current_stage_id", nullable = false)
    private PipelineStage currentStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private CandidateSource source;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "cv_storage_path", columnDefinition = "TEXT")
    private String cvStoragePath;

    @Column(name = "cover_letter_storage_path", columnDefinition = "TEXT")
    private String coverLetterStoragePath;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    // --- Relations ---

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RecruiterNote> recruiterNotes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ApplicantTag> tags = new LinkedHashSet<>();
}
