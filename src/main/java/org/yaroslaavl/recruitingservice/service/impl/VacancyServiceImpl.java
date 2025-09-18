package org.yaroslaavl.recruitingservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.broker.RecruitingAppNotificationEventPublisher;
import org.yaroslaavl.recruitingservice.database.entity.enums.*;
import org.yaroslaavl.recruitingservice.dto.request.VacancyUpdateRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.VacancyShortDto;
import org.yaroslaavl.recruitingservice.exception.CreationFailedException;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.exception.RecruiterNotBelongToCompanyOrVacancyException;
import org.yaroslaavl.recruitingservice.feignClient.dto.CompanyPreviewFeignDto;
import org.yaroslaavl.recruitingservice.mapper.VacancyMapper;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;
import org.yaroslaavl.recruitingservice.service.VacancyService;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;
import org.yaroslaavl.recruitingservice.util.NotificationStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    private final VacancyMapper vacancyMapper;
    private final VacancyRepository vacancyRepository;
    private final SecurityContextService securityContextService;
    private final UserFeignClient userFeignClient;
    private final RecruitingAppNotificationEventPublisher publisher;

    /**
     * Creates a new vacancy based on the provided vacancy request data.
     * The method validates the recruiter belongs to the specified company
     * and initializes the vacancy with default properties such as status
     * and approval status before saving it to the repository.
     *
     * @param vacancyRequestDto the data transfer object containing the required details
     *                          for creating a new vacancy, including company ID, title,
     *                          description, and other relevant fields
     *
     * @throws RecruiterNotBelongToCompanyOrVacancyException if the recruiter does not belong to the provided company
     * @throws CreationFailedException if the creation process fails due to any exception
     */
    @Override
    @Transactional
    public void create(VacancyRequestDto vacancyRequestDto) {
        log.info("Creating vacancy: {}", vacancyRequestDto);

        String recruiterKeyId = checkRecruiterCompanyAndVacancyReturnRecruiterId(vacancyRequestDto.companyId(), Boolean.FALSE, null);

        try {
            Vacancy entity = vacancyMapper.toEntity(vacancyRequestDto);
            entity.setRecruiterId(recruiterKeyId);
            entity.setStatus(VacancyStatus.DISABLED);
            entity.setWaitingForApproval(Boolean.TRUE);
            entity.setLastStatusChangeAt(LocalDateTime.now());

            Vacancy vacancy = vacancyRepository.saveAndFlush(entity);
            log.info("Created vacancy: {}", entity.getId());

            publisher.publishInAppNotification(NotificationStore.inAppNotification(null, recruiterKeyId, String.valueOf(entity.getId()), "VACANCY_CREATED", Map.of( "vacancyTitle", vacancy.getTitle(), "createdAt", vacancy.getCreatedAt().toString())));
        } catch (Exception e) {
            log.error("Failed to create vacancy: {}", vacancyRequestDto, e);
            throw new CreationFailedException("Failed to create vacancy");
        }
    }

    /**
     * Deletes a vacancy associated with the specified vacancy ID and company ID.
     * Verifies the recruiter belongs to the specified company and has access to the vacancy
     * before deleting the vacancy from the repository.
     *
     * @param vacancyId the unique identifier of the vacancy to be deleted
     * @param companyId the unique identifier of the company to which the vacancy belongs
     *
     * @throws RecruiterNotBelongToCompanyOrVacancyException if the recruiter does not belong to the provided company or vacancy
     * @throws EntityNotFoundException if the vacancy with the specified ID does not exist
     */
    @Transactional
    public void delete(UUID vacancyId, UUID companyId) {
        String recruiterKeyId = checkRecruiterCompanyAndVacancyReturnRecruiterId(companyId, Boolean.TRUE, vacancyId);

        vacancyRepository.deleteById(vacancyId);
        log.info("Deleted vacancy: {} by recruiter with id: {}]", vacancyId, recruiterKeyId);
    }

    /**
     * Updates the details of an existing vacancy based on the provided update request data.
     * Validates that the recruiter belongs to the specified company and is associated
     * with the vacancy before performing the update.
     *
     * @param vacancyUpdateRequestDto the data transfer object containing the updated details
     *                                of the vacancy, including the vacancy ID, company ID,
     *                                and other relevant fields
     *
     * @throws RecruiterNotBelongToCompanyOrVacancyException if the recruiter does not belong
     *         to the provided company or vacancy
     * @throws EntityNotFoundException if the vacancy with the specified ID does not exist
     */
    @Override
    @Transactional
    public void update(UUID vacancyId, VacancyUpdateRequestDto vacancyUpdateRequestDto) {
        log.info("Updating vacancy: {}", vacancyUpdateRequestDto);

        String recruiterKeyId = checkRecruiterCompanyAndVacancyReturnRecruiterId(vacancyUpdateRequestDto.companyId(), Boolean.TRUE, vacancyId);

        vacancyMapper.updateEntity(vacancyUpdateRequestDto, vacancyRepository.findById(vacancyId).orElseThrow());
        log.info("Updated vacancy by recruiter with id: {}", recruiterKeyId);
    }

    @Override
    public VacancyResponseDto getVacancy(UUID vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId).orElseThrow(
                () -> new EntityNotFoundException("Vacancy with id: " + vacancyId + " not found"));

        return vacancyMapper.toDto(vacancy);
    }

    /**
     * Retrieves a filtered list of vacancies based on specified criteria.
     *
     * @param textSearch   the text to search within recruiting titles or category
     * @param contractType the type of contract (e.g., FULL_TIME, PART_TIME) to filter vacancies by
     * @param workMode     the mode of work (e.g., REMOTE, ONSITE, HYBRID) to filter vacancies by
     * @param positionLevel the position level (e.g., JUNIOR, MID, SENIOR) to filter vacancies by
     * @param workload     the workload (e.g., FULL, PART_TIME) to filter vacancies by
     * @param salaryFrom   the minimum salary threshold for filtering vacancies
     * @param salaryTo     the maximum salary threshold for filtering vacancies
     * @param pageable     the pagination information for retrieving paginated results
     *
     * @return a paginated DTO containing a list of filtered vacancies, each represented by a VacancyShortDto
     */
    @Override
    public PageShortDto<VacancyShortDto> getFilteredVacancies(String textSearch, ContractType contractType, WorkMode workMode, PositionLevel positionLevel, Workload workload, Integer salaryFrom, Integer salaryTo, LocalDateTime uploadAt, Pageable pageable) {
        log.info("Getting vacancies by filtered textSearch: {}, contractType: {}, workMode: {}, position: {}, workload: {}",
                textSearch, contractType, workMode, positionLevel, workload);

        LocalDateTime selectedDateStart;
        LocalDateTime selectedDateEnd;

        if (uploadAt != null) {
            selectedDateStart = uploadAt.toLocalDate().atStartOfDay();
            selectedDateEnd = selectedDateStart.toLocalDate().atTime(LocalTime.MAX);
        } else {
            selectedDateStart = LocalDate.of(2025, 1, 1).atStartOfDay();
            selectedDateEnd = LocalDateTime.now();
        }

        Page<Vacancy> filteredVacancies = vacancyRepository.getFilteredVacancies(textSearch, contractType, workMode, positionLevel, workload, salaryFrom, salaryTo, selectedDateStart, selectedDateEnd, pageable);

        if (filteredVacancies.isEmpty()) {
            log.info("No vacancies found");
            return new PageShortDto<>(Collections.emptyList(), 0, 0, 0, 0);
        }

        Map<UUID, CompanyPreviewFeignDto> companyPreview = userFeignClient.previewInfo(filteredVacancies.getContent()
                .stream()
                .map(Vacancy::getCompanyId)
                .collect(Collectors.toSet()));

        return new PageShortDto<>(
                vacancyMapper.toShortDto(filteredVacancies.getContent(), companyPreview),
                filteredVacancies.getTotalElements(),
                filteredVacancies.getTotalPages(),
                filteredVacancies.getNumber(),
                filteredVacancies.getSize());
    }

    /**
     * Retrieves a paginated list of vacancies for a specific company.
     *
     * @param companyId the unique identifier of the company whose vacancies are to be retrieved
     * @param pageable the pagination information, including page number and page size
     * @return a PageShortDto containing a list of VacancyShortDto objects, total items, total pages,
     *         current page number, and page size
     */
    @Override
    public PageShortDto<VacancyShortDto> getCompanyVacancies(UUID companyId, Pageable pageable) {
        log.info("Getting vacancies by company with id: {}", companyId);

        Page<Vacancy> companyVacancies = vacancyRepository.getCompanyVacancies(companyId, pageable);

        if (companyVacancies.isEmpty()) {
            log.info("No vacancies found by company with id: {}", companyId);
            return new PageShortDto<>(Collections.emptyList(), 0, 0, 0, 0);
        }

        Map<UUID, CompanyPreviewFeignDto> previewInfo = userFeignClient.previewInfo(Set.of(companyId));

        return new PageShortDto<>(
                vacancyMapper.toShortDto(companyVacancies.getContent(), previewInfo),
                companyVacancies.getTotalElements(),
                companyVacancies.getTotalPages(),
                companyVacancies.getNumber(),
                companyVacancies.getSize());
    }

    /**
     * Counts the number of vacancies for each company from the provided set of company IDs.
     * Retrieves the vacancies for the given companies from the repository, groups them by company,
     * and calculates the vacancy count. If a company has no vacancies, its count will be set to zero.
     *
     * @param companyIds a set of UUIDs representing the IDs of the companies whose vacancies are to be counted
     * @return a map where the key is the company ID (UUID) and the value is the count of vacancies (Long) for that company
     */
    @Override
    public Map<UUID, Long> countCompanyVacancies(Set<UUID> companyIds) {
        List<Vacancy> companyVacancies = vacancyRepository.getCompaniesVacancy(companyIds);

        Map<UUID, Long> result = companyVacancies.stream().collect(Collectors.groupingBy(Vacancy::getCompanyId, Collectors.counting()));
        companyIds.forEach(companyId -> result.putIfAbsent(companyId, 0L));

        return result;
    }

    /**
     * Validates if the recruiter belongs to the specified company and optionally checks if the recruiter is associated
     * with the specified vacancy. Returns the recruiter's identifier if validation is successful.
     *
     * @param companyId the unique identifier of the company to verify the recruiter's association
     * @param checkVacancy a boolean flag indicating whether to additionally verify the recruiter's association with a specific vacancy
     * @param vacancyId the unique identifier of the vacancy to verify the recruiter's association (used only if checkVacancy is true)
     *
     * @return the identifier of the recruiter if validation is successful
     *
     * @throws RecruiterNotBelongToCompanyOrVacancyException if the recruiter does not belong to the provided company or the specified vacancy when applicable
     * @throws EntityNotFoundException if the vacancy with the specified ID does not exist
     */
    private String checkRecruiterCompanyAndVacancyReturnRecruiterId(UUID companyId, boolean checkVacancy, UUID vacancyId) {
        String recruiterKeyId = securityContextService.getSecurityContext(Credentials.SUB);
        boolean isConnected = userFeignClient.isRecruiterBelongToCompany(
                recruiterKeyId,
                companyId);

        if (!isConnected) {
            throw new RecruiterNotBelongToCompanyOrVacancyException("Recruiter is not belong to company");
        }

        if (checkVacancy) {
            Vacancy vacancy = vacancyRepository.findById(vacancyId).orElseThrow(
                    () -> new EntityNotFoundException("Vacancy with id: " + vacancyId + " not found"));

            if (vacancy.getRecruiterId() != null && vacancy.getRecruiterId().equals(recruiterKeyId)) {
                return vacancy.getRecruiterId();
            }

            throw new RecruiterNotBelongToCompanyOrVacancyException("Recruiter is not belong to company");
        }

        return recruiterKeyId;
    }
}
