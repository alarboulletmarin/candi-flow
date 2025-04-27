package com.candiflow.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"applicant", "tag"})
@Entity
@Table(name = "applicant_tags")
public class ApplicantTag {

    @EmbeddedId
    private ApplicantTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("applicantId")
    @JoinColumn(name = "applicant_id")
    private OpeningApplicant applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
