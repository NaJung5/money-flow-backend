package com.njung.moneyflow.transaction.service;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.repository.CategoryRepository;
import com.njung.moneyflow.fixture.CategoryFixture;
import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.transaction.dto.TransactionRequest;
import com.njung.moneyflow.transaction.entity.Transaction;
import com.njung.moneyflow.transaction.entity.TransactionType;
import com.njung.moneyflow.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TransactionServiceTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("거래 등록 성공")
    void createTransaction() {
        Category category = CategoryFixture.createExpenseCategory("ActiveCategory");
        categoryRepository.save(category);

        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            10000,
            LocalDate.now(ZoneId.of("Asia/Seoul")),
            category.getId(),
            "거래등록 테스트",
            "부평"
        );

        Long transactionId = transactionService.create(request);

        Transaction result = transactionRepository.findById(transactionId).orElseThrow();

        assertThat(result.getId()).isEqualTo(transactionId);
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getCategory().getId()).isEqualTo(category.getId());
        assertThat(result.getMemo()).isEqualTo("거래등록 테스트");
        assertThat(result.getPlace()).isEqualTo("부평");
    }

    @Test
    @DisplayName("삭제된 카테고리로 거래를 등록하면 실패한다")
    void failWhenCategoryIsDeleted() {
        Category category =
            CategoryFixture.createExpenseCategory("ActiveCategory");
        categoryRepository.save(category);

        category.requestDelete();

        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            10000,
            LocalDate.now(ZoneId.of("Asia/Seoul")),
            category.getId(),
            "거래등록 테스트",
            "부평"
        );

        assertThatThrownBy(() -> transactionService.create(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("삭제된 카테고리는 사용할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 거래를 등록하면 실패한다")
    void failWhenCategoryDoseNotExist() {
        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            10000,
            LocalDate.now(ZoneId.of("Asia/Seoul")),
            2139124L,
            "거래등록 테스트",
            "부평"
        );

        assertThatThrownBy(() -> transactionService.create(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("카테고리를 찾을 수 없습니다. categoryId=2139124");
    }
}
