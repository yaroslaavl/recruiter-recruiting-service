package org.yaroslaavl.recruitingservice.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.yaroslaavl.recruitingservice.database.entity.ReportSystem;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportSystemRepository extends JpaRepository<ReportSystem, UUID> {

    @Query("""
    SELECT CASE WHEN COUNT(rs) > 0 THEN TRUE ELSE FALSE END
    FROM ReportSystem rs
    WHERE rs.userId = :userId AND rs.vacancy.id = :vacancyId
    AND rs.status = org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus.NEW
    """)
    boolean existsReportSystemByUserIdAndVacancy_IdAndStatus_New(String userId, UUID vacancyId);

    @Query("""
    SELECT COUNT(rs) FROM ReportSystem rs
    WHERE rs.userId = :userId
    AND rs.createdAt >= :timeSpan
    """)
    long countReportSystemByUserIdWithinTimeSpan(String userId, LocalDateTime timeSpan);

    Optional<ReportSystem> findFirstByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(String userId, LocalDateTime timeSpan);

    Optional<ReportSystem> findReportSystemsByUserIdAndVacancy_Id(String userId, UUID vacancyId);

    @Query(value = """
    SELECT rs FROM ReportSystem rs
    WHERE (:status IS NULL OR rs.status = :status)
    ORDER BY rs.createdAt DESC
    """, countQuery = """
    SELECT COUNT(rs) FROM ReportSystem rs
    WHERE (:status IS NULL OR rs.status = :status)
    """)
    Page<ReportSystem> findReportSystemsByFilteredStatus(RecruitingSystemStatus status, Pageable pageable);

    Optional<ReportSystem> findReportSystemsByUserIdAndId(String userId, UUID id);

    @Query(value = """
    SELECT rs FROM ReportSystem rs
    JOIN FETCH rs.vacancy v
    WHERE (:userId IS NULL OR rs.userId = :userId)
    ORDER BY rs.createdAt DESC
    """, countQuery = """
    SELECT COUNT(rs) FROM ReportSystem rs
    JOIN rs.vacancy v
    WHERE (:userId IS NULL OR rs.userId = :userId)
    """)
    Page<ReportSystem> findReportSystemsByUserId(String userId, Pageable pageable);

    @Query("""
    select r from ReportSystem r
    left join fetch r.vacancy
    where r.id = :id
    """)
    Optional<ReportSystem> findByIdWithVacancy(@Param("id") UUID id);
}
