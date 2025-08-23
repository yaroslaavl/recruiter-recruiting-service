package org.yaroslaavl.recruitingservice.database.entity;

import jakarta.persistence.*;
import jakarta.ws.rs.DefaultValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;
import org.yaroslaavl.recruitingservice.database.entity.enums.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vacancy", schema = "recruiting_data")
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "recruiter_id", nullable = false)
    private String recruiterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements_must_have", columnDefinition = "TEXT", length = 500)
    private String requirementsMustHave;

    @Column(name = "requirements_nice_to_have", columnDefinition = "TEXT", length = 500)
    private String requirementsNiceToHave;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 50)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", length = 50)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_level", length = 50)
    private PositionLevel positionLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "workload", length = 50)
    private Workload workload;

    @Column(name = "location")
    private String location;

    @Column(name = "salary_from")
    private Integer salaryFrom;

    @Column(name = "salary_to")
    private Integer salaryTo;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private VacancyStatus status = VacancyStatus.DISABLED;

    @Column(name = "is_waiting_for_approval", nullable = false)
    private boolean isWaitingForApproval;

    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportSystem> reports;

    @Formula("(select count(*) from recruiting_data.report_system rc where rc.vacancy_id = id and rc.status != 'RESOLVED')")
    private Long notResolvedReports;

    @Column(name = "last_status_changed_at", nullable = false)
    private LocalDateTime lastStatusChangeAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
