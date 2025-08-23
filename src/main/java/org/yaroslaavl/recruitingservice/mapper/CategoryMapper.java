package org.yaroslaavl.recruitingservice.mapper;

import org.mapstruct.Mapper;
import org.yaroslaavl.recruitingservice.database.entity.Category;
import org.yaroslaavl.recruitingservice.dto.response.CategoryResponseDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    List<CategoryResponseDto> toListDto(List<Category> category);
}
