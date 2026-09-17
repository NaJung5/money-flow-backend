package com.njung.moneyflow.transaction.service;

import com.njung.moneyflow.category.entity.Category;
import com.njung.moneyflow.category.repository.CategoryRepository;
import com.njung.moneyflow.transaction.dto.TransactionRequest;
import com.njung.moneyflow.transaction.entity.Money;
import com.njung.moneyflow.transaction.entity.Transaction;
import com.njung.moneyflow.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long create(TransactionRequest request) {
        Category category = categoryRepository
            .findById(request.categoryId())
            .orElseThrow();

        Money money = new Money(request.amount());

        Transaction transaction = new Transaction(
            request.type(),
            money,
            request.transactionDate(),
            category,
            request.memo(),
            request.place()
        );

        Transaction savedTransaction =
            transactionRepository.save(transaction);

        return savedTransaction.getId();
    }
}
