package com.banking.ledger_system.controller;

import com.banking.ledger_system.model.Transaction;
import com.banking.ledger_system.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

class TransferRequest {
    public Long sourceAccountId;
    public Long destinationAccountId;
    public BigDecimal amount;
    public String description;
}

class DepositRequest {
    public Long accountId;
    public BigDecimal amount;
    public String description;
}

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfers")
    public Transaction makeTransfer(@RequestBody TransferRequest request) {
        return transactionService.performTransfer(
            request.sourceAccountId, 
            request.destinationAccountId, 
            request.amount, 
            request.description
        );
    }

    @PostMapping("/deposits")
    public Transaction makeDeposit(@RequestBody DepositRequest request) {
        return transactionService.performDeposit(
            request.accountId, 
            request.amount, 
            request.description
        );
    }
}