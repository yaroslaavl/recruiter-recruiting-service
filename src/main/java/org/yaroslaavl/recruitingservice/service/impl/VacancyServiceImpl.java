package org.yaroslaavl.recruitingservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus;
import org.yaroslaavl.recruitingservice.dto.request.VacancyUpdateRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;
import org.yaroslaavl.recruitingservice.exception.CreationFailedException;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.exception.RecruiterNotBelongToCompanyOrVacancyException;
import org.yaroslaavl.recruitingservice.mapper.VacancyMapper;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;
import org.yaroslaavl.recruitingservice.service.VacancyService;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    private final VacancyMapper vacancyMapper;
    private final VacancyRepository vacancyRepository;
    private final SecurityContextService securityContextService;
    private final UserFeignClient userFeignClient;

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

            vacancyRepository.save(entity);
            log.info("Created vacancy: {}", entity.getId());

            //send notification to Recruiter
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
        //send notification to Recruiter???
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
