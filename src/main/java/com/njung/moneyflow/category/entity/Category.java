package com.njung.moneyflow.category.entity;

import com.njung.moneyflow.transaction.entity.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType categoryType;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    // User Entity 작성 후 연결
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_id")
    // private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public Category(
            String name,
            TransactionType type,
            CategoryType categoryType,
            Category parentCategory
    ) {
        if (type == null) {
            throw new IllegalArgumentException("거래 구분은 필수입니다.");
        }

        if (categoryType == null) {
            throw new IllegalArgumentException("카테고리 구분은 필수입니다.");
        }

        validateName(name);
        validateParentCategory(parentCategory, type);

        this.name = name;
        this.type = type;
        this.categoryType = categoryType;
        this.parentCategory = parentCategory;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public void rename(String name) {
        if (this.categoryType == CategoryType.SYSTEM) {
            throw new IllegalStateException("기본 제공 카테고리는 수정할 수 없습니다.");
        }

        if (this.deletedAt != null) {
            throw new IllegalStateException("삭제된 카테고리는 수정할 수 없습니다.");
        }

        validateName(name);

        this.name = name;
    }

    public void activate() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("삭제된 카테고리는 활성화할 수 없습니다.");
        }

        this.active = true;
    }

    public void deactivate() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("삭제된 카테고리는 비활성화할 수 없습니다.");
        }

        this.active = false;
    }

    public void requestDelete() {
        if (this.categoryType == CategoryType.SYSTEM) {
            throw new IllegalStateException("기본 제공 카테고리는 삭제할 수 없습니다.");
        }

        if (this.deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 카테고리입니다.");
        }

        this.active = false;
        this.deletedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }

        if (name.length() > 50) {
            throw new IllegalArgumentException("카테고리명은 50자를 초과할 수 없습니다.");
        }
    }

    private void validateParentCategory(
            Category parentCategory,
            TransactionType type
    ) {
        if (parentCategory == null) {
            return;
        }

        if (parentCategory.deletedAt != null) {
            throw new IllegalStateException("삭제된 카테고리는 부모 카테고리로 사용할 수 없습니다.");
        }

        if (!parentCategory.active) {
            throw new IllegalStateException("비활성화된 카테고리에는 하위 카테고리를 추가할 수 없습니다.");
        }

        if (parentCategory.parentCategory != null) {
            throw new IllegalStateException("카테고리는 최대 2단계까지만 생성할 수 있습니다.");
        }

        if (parentCategory.type != type) {
            throw new IllegalArgumentException(
                    "부모와 자식 카테고리의 거래 구분이 일치하지 않습니다."
            );
        }
    }
}
