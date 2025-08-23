package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yaroslaavl.recruitingservice.dto.response.CategoryResponseDto;
import org.yaroslaavl.recruitingservice.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/filtered")
    public ResponseEntity<List<CategoryResponseDto>> findFilteredCategories(@RequestParam(value = "searchName", required = false) String searchName) {
        return ResponseEntity.ok(categoryService.findFilteredCategories(searchName));
    }
}
