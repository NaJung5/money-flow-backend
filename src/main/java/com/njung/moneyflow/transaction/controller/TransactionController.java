package com.njung.moneyflow.transaction.controller;

import com.njung.moneyflow.transaction.dto.TransactionRequest;
import com.njung.moneyflow.transaction.dto.TransactionResponse;
import com.njung.moneyflow.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request) {
        Long transactionId = transactionService.create(request);

        return new TransactionResponse(transactionId);
    }
}
