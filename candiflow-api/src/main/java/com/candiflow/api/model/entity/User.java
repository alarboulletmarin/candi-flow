package com.candiflow.api.model.entity;

import com.candiflow.api.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"applications", "jobOpenings", "recruiterNotes"})
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "name")
    private String name;

    // --- Relations ---

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Application> applications = new LinkedHashSet<>();

    @OneToMany(mappedBy = "recruiter", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<JobOpening> jobOpenings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<RecruiterNote> recruiterNotes = new LinkedHashSet<>();
}
