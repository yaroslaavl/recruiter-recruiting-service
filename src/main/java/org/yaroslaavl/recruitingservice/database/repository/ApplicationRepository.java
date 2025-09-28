package org.yaroslaavl.recruitingservice.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.yaroslaavl.recruitingservice.database.entity.Application;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByVacancyIdAndCandidateId(UUID vacancyId, String cvId);

    @Query("""
    SELECT app FROM Application app
    WHERE app.vacancy.id = :vacancyId
    AND (:status IS NULL OR app.status = :status)
    AND (:userFilteredIds IS NULL OR app.candidateId IN (:userFilteredIds))
    """)
    Page<Application> findApplicationsByVacancyIdAndStatus(UUID vacancyId, RecruitingSystemStatus status, List<String> userFilteredIds, Pageable pageable);

    @Query("""
    SELECT app FROM Application app
    JOIN FETCH app.vacancy v
    WHERE app.candidateId = :candidateId
    ORDER BY app.appliedAt DESC
    """)
    Page<Application> findApplicationsByCandidateId(String candidateId, Pageable pageable);

    @Query("""
    SELECT COUNT(app) FROM Application app
    WHERE app.vacancy.id = :vacancyId
    """)
    int findApplicationsByVacancyId(UUID vacancyId);

    @Query("""
    SELECT app FROM Application app
    JOIN FETCH app.vacancy v
    WHERE (:applicationIds IS NULL OR app.id IN (:applicationIds))
    """)
    List<Application> findAllByApplicationsIds(Set<UUID> applicationIds);
}
