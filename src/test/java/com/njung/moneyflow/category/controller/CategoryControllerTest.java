package com.njung.moneyflow.category.controller;

import com.njung.moneyflow.category.dto.CategoryRequest;
import com.njung.moneyflow.category.dto.CategoryUpdateRequest;
import com.njung.moneyflow.category.service.CategoryService;
import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    @DisplayName("카테고리의 ID를 받아 삭제하면 204를 반환한다")
    void deleteCategory() throws Exception {
        // when & then
        mockMvc
            .perform(delete("/api/category/{categoryId}", 1L))
            .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리를 삭제하면 404와 상세 정보가 제외된 메시지를 반환한다")
    void failDeleteWhenCategoryDoesNotExist() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "categoryId=999"))
            .when(categoryService).delete(999L);

        // when & then
        mockMvc
            .perform(delete("/api/category/{categoryId}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("카테고리를 찾을 수 없습니다."));

        verify(categoryService).delete(999L);
    }

    @Test
    @DisplayName("시스템 카테고리를 삭제하면 403을 반환한다")
    void failDeleteWhenSystemCategory() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.CATEGORY_SYSTEM_DELETE_NOT_ALLOWED))
            .when(categoryService).delete(1L);

        // when & then
        mockMvc
            .perform(delete("/api/category/{categoryId}", 1L))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("시스템 카테고리는 삭제할 수 없습니다."));

        verify(categoryService).delete(1L);
    }

    @Test
    @DisplayName("하위 카테고리가 존재하는 카테고리를 삭제하면 409를 반환한다")
    void failDeleteWhenCategoryHasChildren() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN))
            .when(categoryService).delete(1L);

        // when & then
        mockMvc
            .perform(delete("/api/category/{categoryId}", 1L))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("하위 카테고리가 존재하여 삭제할 수 없습니다."));

        verify(categoryService).delete(1L);
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
