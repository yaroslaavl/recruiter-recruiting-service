package org.yaroslaavl.recruitingservice.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.yaroslaavl.recruitingservice.database.entity.enums.ApplicationStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "application_history", schema = "recruiting_data")
public class ApplicationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private ApplicationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private ApplicationStatus newStatus;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime changedAt;
}