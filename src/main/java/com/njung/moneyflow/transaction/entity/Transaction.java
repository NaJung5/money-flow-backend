package com.njung.moneyflow.transaction.entity;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
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
            throw new BusinessException(ErrorCode.TRANSACTION_DELETED);
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
            throw new BusinessException(ErrorCode.TRANSACTION_DELETED);
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
            throw new BusinessException(ErrorCode.INVALID_INPUT, "type=null");
        }
        if (type != category.getType()) {
            throw new BusinessException(
                ErrorCode.TRANSACTION_CATEGORY_TYPE_MISMATCH,
                "type=" + type + ", categoryType=" + category.getType()
            );
        }
    }

    private void validateMemo(String memo) {
        if (memo != null && memo.length() > 50) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "memo.length=" + memo.length());
        }
    }

    private void validatePlace(String place) {
        if (place != null && place.length() > 50) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "place.length=" + place.length());
        }
    }

    private void validateCategory(Category category) {

        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "category=null");
        }


        if (category.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.CATEGORY_DELETED);
        }


        if (!category.isActive()) {
            throw new BusinessException(ErrorCode.CATEGORY_INACTIVE, "categoryId=" + category.getId());
        }
    }

    private void validateAmount(Money amount) {
        if (amount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "amount=null");
        }
    }
}
