package com.njung.moneyflow.category.entity;

import com.njung.moneyflow.transaction.entity.TransactionType;

import java.time.LocalDateTime;

public class Category {

    private Long id;

    private String name;

    private TransactionType type;

    private CategoryType categoryType;

    private boolean active;

    private Category parentCategory;

//    private User user;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}