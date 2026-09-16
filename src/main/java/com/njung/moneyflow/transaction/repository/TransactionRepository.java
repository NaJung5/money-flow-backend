package com.njung.moneyflow.transaction.repository;

import com.njung.moneyflow.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
