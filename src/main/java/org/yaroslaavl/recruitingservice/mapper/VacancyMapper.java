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

@Mapper(componentModel = "spring")
public interface VacancyMapper {

    @Mapping(target = "category.id", source = "categoryId")
    Vacancy toEntity(VacancyRequestDto vacancyRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(VacancyUpdateRequestDto vacancyUpdateRequestDto, @MappingTarget Vacancy vacancy);

    @Mapping(target = "category", source = "category.name")
    VacancyResponseDto toDto(Vacancy vacancy);

    @Mapping(target = "category", source = "vacancy.category.name")
    @Mapping(target = "companyId", expression = "java(getCompanyId(vacancy, previewInfo))")
    @Mapping(target = "companyName", expression = "java(getCompanyName(vacancy, previewInfo))")
    @Mapping(target = "companyLogoUrl", expression = "java(getCompanyLogoUrl(vacancy, previewInfo))")
    @Mapping(target = "companyLocation", expression = "java(getCompanyLocation(vacancy, previewInfo))")
    VacancyShortDto toShortDto(Vacancy vacancy, Map<UUID, CompanyPreviewFeignDto> previewInfo);

    default List<VacancyShortDto> toShortDto(List<Vacancy> vacancies, Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        return vacancies.stream()
                .map(vacancy -> toShortDto(vacancy, previewInfo))
                .toList();
    }

    default String getCompanyName(Vacancy vacancy, Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        CompanyPreviewFeignDto preview = previewInfo.get(vacancy.getCompanyId());
        return preview != null ? preview.name() : null;
    }

    default String getCompanyLogoUrl(Vacancy vacancy,  Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        CompanyPreviewFeignDto preview = previewInfo.get(vacancy.getCompanyId());
        return preview != null ? preview.logoUrl() : null;
    }

    default UUID getCompanyId(Vacancy vacancy,  Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        CompanyPreviewFeignDto preview = previewInfo.get(vacancy.getCompanyId());
        return preview != null ? preview.id() : null;
    }

    default String getCompanyLocation(Vacancy vacancy, Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        CompanyPreviewFeignDto companyPreviewFeignDto = previewInfo.get(vacancy.getCompanyId());
        return companyPreviewFeignDto != null ? companyPreviewFeignDto.location() : null;
    }
}
