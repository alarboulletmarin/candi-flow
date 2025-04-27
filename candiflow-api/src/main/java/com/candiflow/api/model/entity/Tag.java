package com.candiflow.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"applicationTags", "applicantTags"})
@Entity
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tags_name", columnNames = "name")
})
@AttributeOverride(name = "id", column = @Column(name = "tag_id"))
public class Tag extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name; // Ex: 'React', 'Java', 'Senior'

    // --- Relations ---

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ApplicationTag> applicationTags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ApplicantTag> applicantTags = new LinkedHashSet<>();
}
