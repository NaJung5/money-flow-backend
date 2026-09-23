package com.njung.moneyflow.transaction.controller;

import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
import com.njung.moneyflow.transaction.dto.TransactionRequest;
import com.njung.moneyflow.transaction.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    @DisplayName("거래 등록 요청이 정상적이면 201과 거래 ID를 반환한다")
    void createTransaction() throws Exception {
        when(transactionService.create(any(TransactionRequest.class)))
            .thenReturn(1L);

        String requestBody = """
            {
              "type": "EXPENSE",
              "amount": 10000,
              "transactionDate": "2026-09-17",
              "categoryId": 1,
              "memo": "거래등록 테스트",
              "place": "부평"
            }
            """;

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionId").value(1L));

        verify(transactionService)
            .create(any(TransactionRequest.class));
    }

    @Test
    @DisplayName("거래 금액이 0이면 400을 반환하고 서비스를 호출하지 않는다")
    void failWhenAmountIsZero() throws Exception {
        String requestBody = """
            {
              "type": "EXPENSE",
              "amount": 0,
              "transactionDate": "2026-09-17",
              "categoryId": 1,
              "memo": "거래등록 테스트",
              "place": "부평"
            }
            """;

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 거래 등록 시 404를 반환한다")
    void failWhenCategoryDoesNotExist() throws Exception {
        when(transactionService.create(any(TransactionRequest.class)))
            .thenThrow(new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "999"));

        String requestBody = """
            {
              "type": "EXPENSE",
              "amount": 10000,
              "transactionDate": "2026-09-17",
              "categoryId": 999,
              "memo": "거래등록 테스트",
              "place": "부평"
            }
            """;

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value("카테고리를 찾을 수 없습니다. categoryId=999"));

        verify(transactionService)
            .create(any(TransactionRequest.class));
    }

    @Test
    @DisplayName("삭제 된 카테고리는 400을 반환한다.")
    void failWhenCategory() throws Exception {
        when(transactionService.create(any(TransactionRequest.class)))
            .thenThrow(new BusinessException(ErrorCode.CATEGORY_DELETED));

        String requestBody = """
            {
              "type": "EXPENSE",
              "amount": 10000,
              "transactionDate": "2026-09-17",
              "categoryId": 999,
              "memo": "거래등록 테스트",
              "place": "부평"
            }
            """;

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value("카테고리를 찾을 수 없습니다. categoryId=999"));

        verify(transactionService)
            .create(any(TransactionRequest.class));
    }
}
