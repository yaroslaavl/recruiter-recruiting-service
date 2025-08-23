package org.yaroslaavl.recruitingservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus;
import org.yaroslaavl.recruitingservice.exception.CreationFailedException;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.exception.RecruiterNotBelongToCompanyException;
import org.yaroslaavl.recruitingservice.mapper.VacancyMapper;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;
import org.yaroslaavl.recruitingservice.service.VacancyService;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    @Value("${feign.token-type}")
    private String tokenType;
    private final VacancyMapper vacancyMapper;
    private final VacancyRepository vacancyRepository;
    private final SecurityContextService securityContextService;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public void createVacancy(VacancyRequestDto vacancyRequestDto) {
        log.info("Creating vacancy: {}", vacancyRequestDto);

        String recruiterKeyId = securityContextService.getSecurityContext(Credentials.SUB);
        boolean isConnected = userFeignClient.isRecruiterBelongToCompany(
                tokenType + " " +
                securityContextService.getSecurityContext(Credentials.TOKEN),
                recruiterKeyId,
                vacancyRequestDto.companyId());

        if (!isConnected) {
            throw new RecruiterNotBelongToCompanyException("Recruiter is not belong to company");
        }

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

}
