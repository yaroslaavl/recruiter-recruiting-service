package org.yaroslaavl.recruitingservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;

@Mapper(componentModel = "spring")
public interface VacancyMapper {

    @Mapping(target = "category.id", source = "categoryId")
    Vacancy toEntity(VacancyRequestDto vacancyRequestDto);
}
