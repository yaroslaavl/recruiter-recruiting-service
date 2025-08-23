package org.yaroslaavl.recruitingservice.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;

import java.util.List;
import java.util.UUID;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, UUID> {

    @Query("""
    SELECT v FROM Vacancy v
    WHERE v.isWaitingForApproval = true
    AND v.status = 'DISABLED'
    ORDER BY v.createdAt ASC
    """)
    List<Vacancy> findAllNotActiveVacancies();

    @Query("""
    SELECT v FROM Vacancy v
    WHERE v.isWaitingForApproval = false
    AND v.status = 'ENABLED'
    ORDER BY v.createdAt ASC
    """)
    List<Vacancy> findAllActiveVacancies();
}
