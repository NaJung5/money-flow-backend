package com.njung.moneyflow.transaction.entity;

import com.njung.moneyflow.category.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Embedded
    private Money amount;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 50)
    private String memo;

    @Column(length = 50)
    private String place;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public Transaction(TransactionType type,
                       Money amount,
                       LocalDate transactionDate,
                       Category category,
                       String memo,
                       String place) {
        validateMemo(memo);
        validatePlace(place);
        validateCategory(category);
        validateTypeMatch(type, category);
        validateAmount(amount);


        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate == null ? LocalDate.now(ZoneId.of("Asia/Seoul")) : transactionDate;
        this.category = category;
        this.memo = memo;
        this.place = place;

    }

    public void delete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("이미 삭제 된 거래입니다.");
        }
        this.deletedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }


    public void change(Money amount,
                       LocalDate transactionDate,
                       Category category,
                       String memo,
                       String place) {
        if (this.deletedAt != null) {
            throw new IllegalStateException("삭제된 거래는 수정이 불가능 합니다.");
        }

        validateMemo(memo);
        validatePlace(place);
        validateCategory(category);
        validateAmount(amount);
        validateTypeMatch(this.type, category);

        this.amount = amount;
        this.transactionDate = transactionDate == null ? this.transactionDate : transactionDate;
        this.category = category;
        this.memo = memo;
        this.place = place;
    }

    private void validateTypeMatch(TransactionType type, Category category) {
        if (type == null) {
            throw new IllegalArgumentException("입출금 값은 필수입니다.");
        }
        if (type != category.getType()) {
            throw new IllegalArgumentException("입출금 구분은 같아야 합니다.");
        }
    }

    private void validateMemo(String memo) {
        if (memo != null && memo.length() > 50) {
            throw new IllegalArgumentException("메모는 50자를 초과할 수 없습니다.");
        }
    }

    private void validatePlace(String place) {
        if (place != null && place.length() > 50) {
            throw new IllegalArgumentException("장소는 50자를 초과할 수 없습니다.");
        }
    }

    private void validateCategory(Category category) {

        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }

        if (!category.isActive()) {
            throw new IllegalStateException("비활성화된 카테고리는 사용이 불가능 합니다.");
        }
    }

    private void validateAmount(Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException("금액은 필수 항목입니다.");
        }
    }
}
