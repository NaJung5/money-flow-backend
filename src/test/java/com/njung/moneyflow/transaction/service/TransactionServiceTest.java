package com.njung.moneyflow.transaction.service;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.repository.CategoryRepository;
import com.njung.moneyflow.fixture.CategoryFixture;
import com.njung.moneyflow.fixture.TransactionFixture;
import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
import com.njung.moneyflow.transaction.dto.TransactionRequest;
import com.njung.moneyflow.transaction.dto.TransactionUpdateRequest;
import com.njung.moneyflow.transaction.entity.Transaction;
import com.njung.moneyflow.transaction.entity.TransactionType;
import com.njung.moneyflow.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    // ==================== 생성 테스트 ====================

    @Test
    @DisplayName("거래 등록 성공")
    void createTransaction() {
        // given
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

        // when
        Long transactionId = transactionService.create(request);

        Transaction result = transactionRepository.findById(transactionId).orElseThrow();

        // then
        assertThat(result.getId()).isEqualTo(transactionId);
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getCategory().getId()).isEqualTo(category.getId());
        assertThat(result.getMemo()).isEqualTo("거래등록 테스트");
        assertThat(result.getPlace()).isEqualTo("부평");
    }

    @Test
    @DisplayName("삭제된 카테고리로 거래를 등록하면 실패한다")
    void failWhenCategoryIsDeleted() {
        // given
        Category category = CategoryFixture.createExpenseCategory("ActiveCategory");
        categoryRepository.save(category);

        category.delete();

        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            10000,
            LocalDate.now(ZoneId.of("Asia/Seoul")),
            category.getId(),
            "거래등록 테스트",
            "부평"
        );

        // when & then
        assertThatThrownBy(() -> transactionService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 거래를 등록하면 실패한다")
    void failWhenCategoryDoesNotExist() {
        // given
        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            10000,
            LocalDate.now(ZoneId.of("Asia/Seoul")),
            2139124L,
            "거래등록 테스트",
            "부평"
        );

        // when & then
        assertThatThrownBy(() -> transactionService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    // ==================== 수정 테스트 ====================

    @Test
    @DisplayName("거래 수정 성공 시 금액, 거래일, 카테고리, 메모, 장소가 반영된다")
    void updateTransaction() {
        // given
        Category category = saveExpenseCategory("updateTestCategory");
        Transaction transaction = saveTransaction(category);

        Category newCategory = saveExpenseCategory("newCategory");
        TransactionUpdateRequest request = new TransactionUpdateRequest(
            10000,
            transaction.getTransactionDate().minusDays(1),
            newCategory.getId(),
            "수정메모",
            "수정장소"
        );

        // when
        transactionService.update(transaction.getId(), request);

        // then
        Transaction result = findTransaction(transaction.getId());

        assertThat(result.getAmount().getAmount()).isEqualTo(request.amount());
        assertThat(result.getTransactionDate()).isEqualTo(request.transactionDate());
        assertThat(result.getCategory().getId()).isEqualTo(request.categoryId());
        assertThat(result.getMemo()).isEqualTo(request.memo());
        assertThat(result.getPlace()).isEqualTo(request.place());
    }

    @Test
    @DisplayName("존재하지 않는 거래를 수정하면 실패한다")
    void failWhenUpdatingNonExistentTransaction() {
        // given
        Category category = saveExpenseCategory("updateTestCategory");
        TransactionUpdateRequest request = updateRequest(category.getId());

        // when & then
        assertThatThrownBy(() -> transactionService.update(Long.MAX_VALUE, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제된 거래를 수정하면 실패한다")
    void failWhenUpdatingDeletedTransaction() {
        // given
        Category category = saveExpenseCategory("updateTestCategory");
        Transaction transaction = saveTransaction(category);
        transaction.delete();

        TransactionUpdateRequest request = updateRequest(category.getId());

        // when & then
        assertThatThrownBy(() -> transactionService.update(transaction.getId(), request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 거래를 수정하면 실패한다")
    void failWhenUpdatingWithNonExistentCategory() {
        // given
        Category category = saveExpenseCategory("originalCategory");
        Transaction transaction = saveTransaction(category);
        TransactionUpdateRequest request = updateRequest(Long.MAX_VALUE);

        // when & then
        assertThatThrownBy(() -> transactionService.update(transaction.getId(), request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제된 카테고리로 거래를 수정하면 실패한다")
    void failWhenUpdatingWithDeletedCategory() {
        // given
        Category category = saveExpenseCategory("originalCategory");
        Transaction transaction = saveTransaction(category);

        Category deletedCategory = saveExpenseCategory("deletedCategory");
        deletedCategory.delete();

        TransactionUpdateRequest request = updateRequest(deletedCategory.getId());

        // when & then
        assertThatThrownBy(() -> transactionService.update(transaction.getId(), request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("비활성 카테고리로 거래를 수정하면 실패한다")
    void failWhenUpdatingWithInactiveCategory() {
        // given
        Category category = saveExpenseCategory("originalCategory");
        Transaction transaction = saveTransaction(category);

        Category inactiveCategory = saveExpenseCategory("inactiveCategory");
        inactiveCategory.deactivate();

        TransactionUpdateRequest request = updateRequest(inactiveCategory.getId());

        // when & then
        assertThatThrownBy(() -> transactionService.update(transaction.getId(), request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CATEGORY_INACTIVE);
    }

    @Test
    @DisplayName("거래 구분과 다른 구분의 카테고리로 수정하면 실패한다")
    void failWhenUpdatingWithDifferentCategoryType() {
        // given
        Category category = saveExpenseCategory("originalCategory");
        Transaction transaction = saveTransaction(category);

        Category incomeCategory = saveIncomeCategory("incomeCategory");
        TransactionUpdateRequest request = updateRequest(incomeCategory.getId());

        // when & then
        assertThatThrownBy(() -> transactionService.update(transaction.getId(), request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRANSACTION_CATEGORY_TYPE_MISMATCH);
    }

    @Test
    @DisplayName("메모와 장소를 null로 수정하면 비워진다")
    void clearMemoAndPlaceWhenUpdatingWithNull() {
        // given
        Category category = saveExpenseCategory("originalCategory");
        Transaction transaction = saveTransaction(category);

        TransactionUpdateRequest request = new TransactionUpdateRequest(
            10000,
            transaction.getTransactionDate(),
            category.getId(),
            null,
            null
        );

        // when
        transactionService.update(transaction.getId(), request);

        // then
        Transaction result = findTransaction(transaction.getId());

        assertThat(result.getMemo()).isNull();
        assertThat(result.getPlace()).isNull();
    }

    @Test
    @DisplayName("거래일 없이 수정하면 기존 거래일이 유지된다")
    void keepTransactionDateWhenUpdatingWithoutDate() {
        // given
        Category category = saveExpenseCategory("originalCategory");
        Transaction transaction = saveTransaction(category);
        LocalDate originalDate = transaction.getTransactionDate();

        TransactionUpdateRequest request = new TransactionUpdateRequest(
            10000,
            null,
            category.getId(),
            "수정메모",
            "수정장소"
        );

        // when
        transactionService.update(transaction.getId(), request);

        // then
        Transaction result = findTransaction(transaction.getId());

        assertThat(result.getTransactionDate()).isEqualTo(originalDate);
        assertThat(result.getAmount().getAmount()).isEqualTo(request.amount());
    }

    @Test
    @DisplayName("거래와 카테고리가 모두 없으면 거래 없음 예외가 먼저 발생한다")
    void failWithTransactionNotFoundWhenBothTransactionAndCategoryDoNotExist() {
        // given
        Long nonExistentTransactionId = Long.MAX_VALUE;
        TransactionUpdateRequest request = updateRequest(Long.MAX_VALUE);

        // when & then
        assertThatThrownBy(() -> transactionService.update(nonExistentTransactionId, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND);
    }

    private Category saveExpenseCategory(String name) {
        Category createCategory = CategoryFixture.createExpenseCategory(name);
        return categoryRepository.save(createCategory);
    }

    private Category saveIncomeCategory(String name) {
        Category createCategory = CategoryFixture.createIncomeCategory(name);
        return categoryRepository.save(createCategory);
    }

    private Transaction saveTransaction(Category category) {
        Transaction transaction = TransactionFixture.createTransaction(category);
        return transactionRepository.save(transaction);
    }

    private TransactionUpdateRequest updateRequest(Long categoryId) {
        return new TransactionUpdateRequest(
            50000,
            LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(7),
            categoryId,
            "수정메모",
            "수정장소"
        );
    }

    private Transaction findTransaction(Long transactionId) {
        entityManager.flush();
        entityManager.clear();
        return transactionRepository.findById(transactionId).orElseThrow();
    }
}
