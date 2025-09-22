package org.yaroslaavl.recruitingservice.mapper.helper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.repository.ApplicationRepository;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApplicationMapperHelper {

    @Value("${vacancy.time_expiration}")
    private Duration vacancyTimeExpiration;

    private final VacancyRepository vacancyRepository;
    private final ApplicationRepository repository;

    @Named("mapVacancyIdToNumberOfApplication")
    public Integer mapVacancyIdToNumberOfApplication(UUID vacancyId) {
        return repository.findApplicationsByVacancyId(vacancyId);
    }

    @Named("mapVacancyIdToVacancyExpirationDate")
    public LocalDateTime mapVacancyIdToVacancyExpirationDate(UUID vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new EntityNotFoundException("Vacancy not found"));

        return vacancy.getCreatedAt().plusDays(vacancyTimeExpiration.toDays());
    }
}
