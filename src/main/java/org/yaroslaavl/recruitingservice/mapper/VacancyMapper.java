package org.yaroslaavl.recruitingservice.mapper;

import org.aspectj.lang.annotation.Before;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.dto.request.VacancyUpdateRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;

@Mapper(componentModel = "spring")
public interface VacancyMapper {

    @Mapping(target = "category.id", source = "categoryId")
    Vacancy toEntity(VacancyRequestDto vacancyRequestDto);

    void updateEntity(VacancyUpdateRequestDto vacancyUpdateRequestDto, Vacancy vacancy);

    VacancyResponseDto toDto(Vacancy vacancy);
}
