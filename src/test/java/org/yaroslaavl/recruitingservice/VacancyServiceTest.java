package org.yaroslaavl.recruitingservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.entity.enums.*;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.VacancyShortDto;
import org.yaroslaavl.recruitingservice.feignClient.dto.CompanyPreviewFeignDto;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;
import org.yaroslaavl.recruitingservice.mapper.VacancyMapper;
import org.yaroslaavl.recruitingservice.service.VacancyService;
import org.yaroslaavl.recruitingservice.service.impl.VacancyServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VacancyServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private UserFeignClient userFeignClient;

    @Mock
    private VacancyMapper vacancyMapper;

    @InjectMocks
    private VacancyServiceImpl vacancyService;

    @Test
    void shouldReturnArchivedVacancy() {
        UUID vacancyId = UUID.randomUUID();

        Vacancy archivedVacancy = new Vacancy();
        archivedVacancy.setId(vacancyId);
        archivedVacancy.setTitle("Old Backend");
        archivedVacancy.setStatus(VacancyStatus.ARCHIVED);

        when(vacancyRepository.getVacancyById(vacancyId))
                .thenReturn(Optional.of(archivedVacancy));

        VacancyResponseDto dto = new VacancyResponseDto(vacancyId,
                "Backend",
                "Old Backend",
                "Backend",
                "Spring",
                "React",
                ContractType.EMPLOYMENT_CONTRACT,
                WorkMode.HYBRID,
                PositionLevel.ASSISTANT,
                Workload.FULL_TIME,
                "Szczecin",
                3000,
                5000,
                VacancyStatus.ARCHIVED,
                LocalDateTime.now());

        when(vacancyMapper.toDto(archivedVacancy)).thenReturn(dto);

        VacancyResponseDto result = vacancyService.getVacancy(vacancyId);

        assertEquals("Old Backend", result.title());
        assertEquals(VacancyStatus.ARCHIVED, result.status());

        verify(vacancyRepository, times(1)).getVacancyById(vacancyId);
        verify(vacancyMapper, times(1)).toDto(archivedVacancy);
    }

    @Test
    void shouldReturnFilteredVacancies() {
        Pageable pageable = PageRequest.of(0, 10);

        UUID companyId = UUID.randomUUID();

        Vacancy vacancy = new Vacancy();
        vacancy.setCompanyId(companyId);
        vacancy.setTitle("Backend");

        Page<Vacancy> vacancyPage =
                new PageImpl<>(List.of(vacancy), pageable, 1);

        when(vacancyRepository.getFilteredVacancies(
                eq("Backend"), any(), any(), any(), any(),
                any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(vacancyPage);

        when(vacancyRepository.getFilteredVacancies(
                eq("Backend2"), any(), any(), any(), any(),
                any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Map<UUID, CompanyPreviewFeignDto> companyMap =
                Map.of(companyId, new CompanyPreviewFeignDto(UUID.fromString("ee221091-9673-43b7-9d65-ff4bf6292788"), "Company", "Szczecin", ""));

        when(userFeignClient.previewInfo(anySet()))
                .thenReturn(companyMap);

        when(vacancyMapper.toShortDto(anyList(), anyMap()))
                .thenReturn(List.of(mock(VacancyShortDto.class)));

        PageShortDto<VacancyShortDto> result =
                vacancyService.getFilteredVacancies(
                        "Backend",
                        null, null, null, null,
                        null, null, null,
                        pageable
                );

        PageShortDto<VacancyShortDto> result2 =
                vacancyService.getFilteredVacancies(
                        "Backend2",
                        null, null, null, null,
                        null, null, null,
                        pageable
                );

        assertEquals(1, result.totalElements());
        assertEquals(0, result2.totalElements());
        assertEquals(1, result.allContent().size());
    }
}
