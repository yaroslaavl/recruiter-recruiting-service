package org.yaroslaavl.recruitingservice.util;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaroslaavl.recruitingservice.database.entity.Application;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.database.repository.ApplicationRepository;
import org.yaroslaavl.recruitingservice.database.repository.ReportSystemRepository;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;

import java.util.UUID;

@Component("accessChecker")
@RequiredArgsConstructor
public class AccessChecker {

    private final SecurityContextService securityContextService;
    private final ReportSystemRepository reportSystemRepository;
    private final ApplicationRepository applicationRepository;

    public boolean hasAccessToReport(UUID id) {
        String userId = securityContextService.getSecurityContext(Credentials.SUB);
        return userId != null && !userId.isEmpty()
                && reportSystemRepository.findReportSystemsByUserIdAndId(userId, id).isPresent();
    }

    public boolean hasAccessToApplication(UUID id) {
        ApplicationInfo info = shortInfo(id);
        return info.application.getCandidateId().equals(info.userId) ||
                info.application.getVacancy().getRecruiterId().equals(info.userId);
    }

    public boolean hasAccessToChangeApplicationStatus(UUID id) {
        ApplicationInfo applicationInfo = shortInfo(id);
        return applicationInfo.application.getVacancy().getRecruiterId().equals(applicationInfo.userId);
    }

    private ApplicationInfo shortInfo(UUID id) {
        String userId = securityContextService.getSecurityContext(Credentials.SUB);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application Not Found"));
        return new ApplicationInfo(userId, application);
    }

    private record ApplicationInfo (
            @NotBlank String userId,
            @NotNull Application application
    ){};
}