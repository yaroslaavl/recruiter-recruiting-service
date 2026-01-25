package org.yaroslaavl.recruitingservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaroslaavl.recruitingservice.broker.RecruitingAppNotificationEventPublisher;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.database.repository.ApplicationHistoryRepository;
import org.yaroslaavl.recruitingservice.database.repository.ApplicationRepository;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;
import org.yaroslaavl.recruitingservice.dto.request.VacancyApplicationRequestDto;
import org.yaroslaavl.recruitingservice.feignClient.cv.CvFeignClient;
import org.yaroslaavl.recruitingservice.feignClient.dto.CVApplicationDto;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;
import org.yaroslaavl.recruitingservice.mapper.VacancyMapper;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;
import org.yaroslaavl.recruitingservice.service.impl.ApplicationServiceImpl;
import org.yaroslaavl.recruitingservice.service.impl.SecurityContextServiceImpl;
import org.yaroslaavl.recruitingservice.service.impl.VacancyServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserFeignClient userFeignClient;

    @Mock
    private CvFeignClient cvFeignClient;

    @Mock
    private RecruitingAppNotificationEventPublisher publisher;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Mock
    private SecurityContextServiceImpl securityContextService;

    @Mock
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Test
    void shouldApplyVacancySuccessfully() {
        UUID vacancyId = UUID.randomUUID();
        UUID cvId = UUID.randomUUID();
        String candidateKeyId = "81c3aaeb-b0f3-4f38-a3bf-f9ce2c22c4e1";

        VacancyApplicationRequestDto requestDto = new VacancyApplicationRequestDto(
                vacancyId, cvId, "My cover letter"
        );

        Vacancy vacancy = new Vacancy();
        vacancy.setId(vacancyId);
        vacancy.setTitle("Backend");

        when(securityContextService.getSecurityContext(Credentials.SUB))
                .thenReturn(candidateKeyId);
        when(userFeignClient.isApproved(candidateKeyId))
                .thenReturn(true);
        when(cvFeignClient.getCvForRecruiter(cvId))
                .thenReturn(new CVApplicationDto(cvId, "test-url", "fileName"));
        when(vacancyRepository.findById(vacancyId))
                .thenReturn(Optional.of(vacancy));
        when(applicationRepository.findByVacancyIdAndCandidateId(vacancyId, candidateKeyId))
                .thenReturn(Optional.empty());

        applicationService.applyVacancy(requestDto);

        verify(applicationRepository, times(1)).save(any());
        verify(publisher, times(1)).publishInAppNotification(any());

        verify(applicationHistoryRepository, times(1))
                .save(argThat(history ->
                        history.getApplication() != null &&
                                history.getNewStatus() == RecruitingSystemStatus.NEW &&
                                "system".equals(history.getChangedBy())
                ));
    }
}
