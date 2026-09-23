package com.njung.moneyflow.category.controller;

import com.njung.moneyflow.category.dto.CategoryRequest;
import com.njung.moneyflow.category.dto.CategoryResponse;
import com.njung.moneyflow.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        Long categoryId = categoryService.create(request);
        return new CategoryResponse(categoryId);
    }
}
