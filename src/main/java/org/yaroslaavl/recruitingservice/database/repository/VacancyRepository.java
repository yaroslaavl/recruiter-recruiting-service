package org.yaroslaavl.recruitingservice.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.entity.enums.ContractType;
import org.yaroslaavl.recruitingservice.database.entity.enums.PositionLevel;
import org.yaroslaavl.recruitingservice.database.entity.enums.WorkMode;
import org.yaroslaavl.recruitingservice.database.entity.enums.Workload;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, UUID> {

    @Query("""
    SELECT v FROM Vacancy v
    WHERE v.isWaitingForApproval = true
    AND v.status = org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus.DISABLED
    ORDER BY v.createdAt ASC
    """)
    List<Vacancy> findAllNotActiveVacancies();

    @Query("""
    SELECT v FROM Vacancy v
    WHERE v.isWaitingForApproval = false
    AND v.status = org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus.ENABLED
    ORDER BY v.createdAt ASC
    """)
    List<Vacancy> findAllActiveVacancies();

    @Query(value = """
    SELECT v FROM Vacancy v
    JOIN FETCH v.category cat
    WHERE v.status = org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus.ENABLED
    AND (:textSearch = ""
        OR LOWER(v.title) LIKE CONCAT('%', LOWER(:textSearch), '%')
        OR LOWER(v.location) LIKE CONCAT('%', LOWER(:textSearch), '%')
        OR LOWER(cat.name) LIKE CONCAT('%', LOWER(:textSearch), '%')
        )
    AND (:contractType IS NULL OR v.contractType = :contractType)
    AND (:workMode IS NULL OR v.workMode = :workMode)
    AND (:positionLevel IS NULL OR v.positionLevel = :positionLevel)
    AND (:workload IS NULL OR v.workload = :workload)
    AND (:salaryFrom IS NULL OR v.salaryFrom >= :salaryFrom)
    AND (:salaryTo IS NULL OR v.salaryTo <= :salaryTo)
    AND v.createdAt >= :selectedDateStart
    AND v.createdAt <= :selectedDateEnd
    ORDER BY v.createdAt DESC, cat.name ASC
    """,
    countQuery = """
    SELECT COUNT(v) FROM Vacancy v
    JOIN v.category cat
    WHERE v.status = org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus.ENABLED
    AND (:textSearch = ""
        OR LOWER(v.title) LIKE CONCAT('%', LOWER(:textSearch), '%')
        OR LOWER(v.location) LIKE CONCAT('%', LOWER(:textSearch), '%')
        OR LOWER(cat.name) LIKE CONCAT('%', LOWER(:textSearch), '%')
        )
    AND (:contractType IS NULL OR v.contractType = :contractType)
    AND (:workMode IS NULL OR v.workMode = :workMode)
    AND (:positionLevel IS NULL OR v.positionLevel = :positionLevel)
    AND (:workload IS NULL OR v.workload = :workload)
    AND (:salaryFrom IS NULL OR v.salaryFrom >= :salaryFrom)
    AND (:salaryTo IS NULL OR v.salaryTo <= :salaryTo)
    AND v.createdAt >= :selectedDateStart
    AND v.createdAt <= :selectedDateEnd
    """)
    Page<Vacancy> getFilteredVacancies(String textSearch,
                                      ContractType contractType,
                                      WorkMode workMode,
                                      PositionLevel positionLevel,
                                      Workload workload,
                                      Integer salaryFrom,
                                      Integer salaryTo,
                                      LocalDateTime selectedDateStart,
                                      LocalDateTime selectedDateEnd,
                                      Pageable pageable);

    @Query("""
    SELECT v FROM Vacancy v
    JOIN FETCH v.category
    WHERE v.companyId = :companyId
    AND v.status = org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus.ENABLED
    """)
    Page<Vacancy> getCompanyVacancies(UUID companyId, Pageable pageable);

    @Query("""
    SELECT v FROM Vacancy v
    WHERE v.companyId IN (:companyIds)
    AND v.status = org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus.ENABLED
    """)
    List<Vacancy> getCompaniesVacancy(Set<UUID> companyIds);
}
