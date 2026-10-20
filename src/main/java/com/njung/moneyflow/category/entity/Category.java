package com.njung.moneyflow.category.entity;

import com.njung.moneyflow.global.exception.BusinessException;
import com.njung.moneyflow.global.exception.ErrorCode;
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
            throw new BusinessException(ErrorCode.INVALID_INPUT, "type=null");
        }

        if (categoryType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "categoryType=null");
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
        validateName(name);
        validateModifiable();

        this.name = name;
    }

    public void activate() {
        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.CATEGORY_DELETED);
        }

        this.active = true;
    }

    public void deactivate() {
        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.CATEGORY_DELETED);
        }

        this.active = false;
    }

    public void delete() {
        if (this.categoryType == CategoryType.SYSTEM) {
            throw new BusinessException(ErrorCode.CATEGORY_SYSTEM_DELETE_NOT_ALLOWED);
        }

        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.CATEGORY_DELETED);
        }

        this.active = false;
        this.deletedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public void changeParentCategory(Category category) {
        if (this == category) {
            throw new BusinessException(ErrorCode.CATEGORY_INVALID_HIERARCHY, "categoryId=" + this.id + ", parentCategoryId=" + category.id);
        }

        validateParentCategory(category, this.type);
        validateModifiable();

        this.parentCategory = category;
    }


    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "name=" + name);
        }

        if (name.length() > 50) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "name.length=" + name.length());
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
            throw new BusinessException(ErrorCode.CATEGORY_DELETED, "parentCategoryId=" + parentCategory.id);
        }

        if (!parentCategory.active) {
            throw new BusinessException(ErrorCode.CATEGORY_INACTIVE, "parentCategoryId=" + parentCategory.id);
        }

        if (parentCategory.parentCategory != null) {
            throw new BusinessException(ErrorCode.CATEGORY_INVALID_HIERARCHY, "parentCategoryId=" + parentCategory.id);
        }

        if (parentCategory.type != type) {
            throw new BusinessException(
                ErrorCode.CATEGORY_PARENT_TYPE_MISMATCH,
                "parentType=" + parentCategory.type + ", type=" + type
            );
        }
    }

    private void validateModifiable() {
        if (this.categoryType == CategoryType.SYSTEM) {
            throw new BusinessException(ErrorCode.CATEGORY_SYSTEM_MODIFY_NOT_ALLOWED);
        }

        if (this.deletedAt != null) {
            throw new BusinessException(ErrorCode.CATEGORY_DELETED);
        }
    }
}
