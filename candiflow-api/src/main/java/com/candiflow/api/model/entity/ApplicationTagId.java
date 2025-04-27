package com.candiflow.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ApplicationTagId implements Serializable {
    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;
}
