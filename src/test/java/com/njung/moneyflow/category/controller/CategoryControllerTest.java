package com.njung.moneyflow.category.controller;

import com.njung.moneyflow.category.dto.CategoryRequest;
import com.njung.moneyflow.category.dto.CategoryUpdateRequest;
import com.njung.moneyflow.category.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void createCategory() throws Exception {
        // given
        when(categoryService.create(any(CategoryRequest.class))).thenReturn(1L);

        String requestBody = """
            {
              "name": "식비",
              "type": "EXPENSE",
              "parentId": null
            }
            """;

        // when & then
        mockMvc
            .perform(post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.categoryId").value(1L));

        verify(categoryService).create(any(CategoryRequest.class));
    }

    @Test
    @DisplayName("카테고리 이름이 50자이면 수정에 성공한다")
    void updateCategoryWithMaxLengthName() throws Exception {
        // given
        String requestBody = updateRequestBody("가".repeat(50));

        // when & then
        mockMvc
            .perform(patch("/api/category/{categoryId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

        verify(categoryService).update(eq(1L), any(CategoryUpdateRequest.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("카테고리 이름이 null이거나 공백이면 400과 필드 에러를 반환하고 서비스를 호출하지 않는다")
    void failUpdateWhenNameIsBlank(String name) throws Exception {
        // given
        String requestBody = updateRequestBody(name);

        // when & then
        mockMvc
            .perform(patch("/api/category/{categoryId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("올바르지 못한 입력값입니다."))
            .andExpect(jsonPath("$.fieldErrors.length()").value(1))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));

        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("카테고리 이름이 51자이면 400과 필드 에러를 반환하고 서비스를 호출하지 않는다")
    void failUpdateWhenNameExceedsMaxLength() throws Exception {
        // given
        String requestBody = updateRequestBody("가".repeat(51));

        // when & then
        mockMvc
            .perform(patch("/api/category/{categoryId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.length()").value(1))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));

        verifyNoInteractions(categoryService);
    }

    private String updateRequestBody(String name) {
        String nameValue = name == null ? "null" : "\"" + name + "\"";
        return """
            {
              "name": %s
            }
            """.formatted(nameValue);
    }
}
