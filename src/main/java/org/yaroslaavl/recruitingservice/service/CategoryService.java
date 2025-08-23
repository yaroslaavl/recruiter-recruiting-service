package org.yaroslaavl.recruitingservice.service;

import org.yaroslaavl.recruitingservice.dto.response.CategoryResponseDto;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDto> findFilteredCategories(String searchName);
}
