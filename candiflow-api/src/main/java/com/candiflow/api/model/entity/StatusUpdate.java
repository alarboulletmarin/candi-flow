package com.candiflow.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"application"})
@Entity
@Table(name = "status_updates")
@AttributeOverride(name = "id", column = @Column(name = "status_update_id"))
public class StatusUpdate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private ApplicationStatus status;

    @Column(name = "event_date", nullable = false)
    private Instant eventDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
