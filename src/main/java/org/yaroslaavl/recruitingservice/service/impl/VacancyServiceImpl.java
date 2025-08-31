package org.yaroslaavl.recruitingservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus;
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

    @Transactional
    public void delete(UUID vacancyId, UUID companyId) {
        String recruiterKeyId = checkRecruiterCompanyAndVacancyReturnRecruiterId(companyId, Boolean.TRUE, vacancyId);

        vacancyRepository.deleteById(vacancyId);
        log.info("Deleted vacancy: {} by recruiter with id: {}]", vacancyId, recruiterKeyId);
        //send notification to Recruiter???
    }

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
                return recruiterKeyId;
            }

            throw new RecruiterNotBelongToCompanyOrVacancyException("Recruiter is not belong to company");
        }

        return recruiterKeyId;
    }
}
