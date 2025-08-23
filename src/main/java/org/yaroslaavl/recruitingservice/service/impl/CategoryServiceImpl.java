package org.yaroslaavl.recruitingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.database.entity.Category;
import org.yaroslaavl.recruitingservice.database.repository.CategoryRepository;
import org.yaroslaavl.recruitingservice.dto.response.CategoryResponseDto;
import org.yaroslaavl.recruitingservice.mapper.CategoryMapper;
import org.yaroslaavl.recruitingservice.service.CategoryService;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponseDto> findFilteredCategories(String searchName) {
        log.info("Filtering categories by name: {}", searchName);

        List<Category> filteredCategories
                = categoryRepository.findFilteredCategories(searchName);

        if (filteredCategories.isEmpty()) {
            log.info("No categories found");
        }

        log.info("Found '{}' categories", filteredCategories
                .stream()
                .map(Category::getName)
                .distinct()
                .count());
        return categoryMapper.toListDto(filteredCategories);
    }
}
