package org.yaroslaavl.recruitingservice.mapper;

import org.mapstruct.*;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.dto.request.VacancyUpdateRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.VacancyShortDto;
import org.yaroslaavl.recruitingservice.feignClient.dto.CompanyPreviewFeignDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {CommonMapper.class})
public interface VacancyMapper {

    @Mapping(target = "category.id", source = "categoryId")
    Vacancy toEntity(VacancyRequestDto vacancyRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(VacancyUpdateRequestDto vacancyUpdateRequestDto, @MappingTarget Vacancy vacancy);

    @Mapping(target = "category", source = "category.name")
    VacancyResponseDto toDto(Vacancy vacancy);

    @Mapping(target = "category", source = "vacancy.category.name")
    @Mapping(target = "companyId", source = "vacancy.companyId", qualifiedByName = "companyId")
    @Mapping(target = "companyName", source = "vacancy.companyId", qualifiedByName = "companyName")
    @Mapping(target = "companyLogoUrl", source = "vacancy.companyId", qualifiedByName = "companyLogoUrl")
    @Mapping(target = "companyLocation", source = "vacancy.companyId", qualifiedByName = "companyLocation")
    VacancyShortDto toShortDto(Vacancy vacancy, @Context Map<UUID, CompanyPreviewFeignDto> previewInfo);

    default List<VacancyShortDto> toShortDto(List<Vacancy> vacancies, Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        return vacancies.stream()
                .map(vacancy -> toShortDto(vacancy, previewInfo))
                .toList();
    }
}
