package com.candiflow.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "application_statuses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_application_statuses_name", columnNames = "name")
})
@AttributeOverride(name = "id", column = @Column(name = "status_id"))
public class ApplicationStatus extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
