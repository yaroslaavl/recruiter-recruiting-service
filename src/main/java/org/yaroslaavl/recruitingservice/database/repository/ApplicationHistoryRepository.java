package org.yaroslaavl.recruitingservice.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.yaroslaavl.recruitingservice.database.entity.ApplicationHistory;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, UUID> {

    Optional<ApplicationHistory> findTopByApplicationIdOrderByChangedAtDesc(UUID applicationId);
}
