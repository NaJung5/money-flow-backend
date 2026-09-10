package com.njung.moneyflow.transaction.entity;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.entity.CategoryType;
import com.njung.moneyflow.fixture.CategoryFixture;
import com.njung.moneyflow.fixture.MoneyFixture;
import com.njung.moneyflow.fixture.TransactionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    @DisplayName("거래 정상 생성")
    void createTransaction() {
        Transaction transaction = TransactionFixture.createTransaction();

        assertThat(transaction.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.getAmount().getAmount()).isEqualTo(100000);
        assertThat(transaction.getMemo()).isEqualTo("점심");
        assertThat(transaction.getPlace()).isEqualTo("식당");
    }

    @Test
    @DisplayName("거래 정보 정상 수정")
    void changeTransaction() {
        Transaction transaction = TransactionFixture.createTransaction();
        Category category = new Category(
            "수정테스트",
            TransactionType.EXPENSE,
            CategoryType.CUSTOM,
            null
        );
        Money amount = new Money(500);
        LocalDate changedDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);

        transaction.change(amount, changedDate, category, "수정테스트", "인천");

        assertThat(transaction.getMemo()).isEqualTo("수정테스트");
        assertThat(transaction.getPlace()).isEqualTo("인천");
        assertThat(transaction.getCategory().getName()).isEqualTo("수정테스트");
        assertThat(transaction.getAmount().getAmount()).isEqualTo(500);
        assertThat(transaction.getTransactionDate()).isEqualTo(changedDate);
    }

    @Test
    @DisplayName("거래 타입과 카테고리 타입이 다르면 실패")
    void failWhenCategoryTypeDoesNotMatch() {
        Money amount = MoneyFixture.createMoney();
        Category category = new Category("test", TransactionType.INCOME, CategoryType.CUSTOM, null);
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));

        assertThatThrownBy(() -> new Transaction(
            TransactionType.EXPENSE,
            amount,
            now,
            category,
            "점심",
            "식당"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("금액이 없으면 거래 생성 실패")
    void failWhenAmountIsNull() {
        Category category = CategoryFixture.createExpenseCategory();
        LocalDate transactionDate = LocalDate.now();

        assertThatThrownBy(() -> new Transaction(
            TransactionType.EXPENSE,
            null,
            transactionDate,
            category,
            "점심",
            "식당"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("카테고리가 없으면 거래 생성 실패")
    void failWhenCategoryIsNull() {
        LocalDate transactionDate = LocalDate.now();
        Money amount = MoneyFixture.createMoney();

        assertThatThrownBy(() -> new Transaction(
            TransactionType.INCOME,
            amount,
            transactionDate,
            null,
            "a",
            "a"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("삭제된 거래는 수정할 수 없음")
    void failWhenChangingDeletedTransaction() {
        Transaction transaction = TransactionFixture.createTransaction();
        Category category = CategoryFixture.createExpenseCategory();
        Money amount = MoneyFixture.createMoney();
        transaction.delete();

        assertThatThrownBy(() -> transaction.change(
            amount,
            null,
            category,
            "수정테스트",
            null
                                                   )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("다른 거래 타입의 카테고리로 수정하면 실패")
    void failWhenChangingToDifferentCategoryType() {
        Transaction transaction = TransactionFixture.createTransaction();
        Money amount = MoneyFixture.createMoney();
        Category category = new Category(
            "SampleCategory",
            TransactionType.INCOME,
            CategoryType.CUSTOM,
            null
        );
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        assertThatThrownBy(() -> transaction.change(
            amount,
            now,
            category,
            "점심",
            "식당"
                                                   )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 삭제된 거래를 다시 삭제하면 실패")
    void failWhenDeletingAlreadyDeletedTransaction() {
        Transaction transaction = TransactionFixture.createTransaction();
        transaction.delete();

        assertThatThrownBy(transaction::delete).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("비활성 카테고리로 거래 생성 시 실패")
    void failWhenCategoryIsInactive() {
        Category category = CategoryFixture.createExpenseCategory();
        Money amount = MoneyFixture.createMoney();
        LocalDate transactionDate = LocalDate.now();

        category.deactivate();

        assertThatThrownBy(() -> new Transaction(
            TransactionType.EXPENSE,
            amount,
            transactionDate,
            category,
            "a",
            "a"
        )).isInstanceOf(IllegalStateException.class);
    }
}
