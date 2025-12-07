package com.banking.ledger_system.service;

import com.banking.ledger_system.model.Account;
import com.banking.ledger_system.model.LedgerEntry;
import com.banking.ledger_system.model.Transaction;
import com.banking.ledger_system.repository.AccountRepository;
import com.banking.ledger_system.repository.LedgerRepository;
import com.banking.ledger_system.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository,
                              LedgerRepository ledgerRepository,
                              AccountRepository accountRepository,
                              AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    // This annotation ensures ACID properties. 
    // Either ALL checks and saves happen, or NONE happen.
    @Transactional
    public Transaction performTransfer(Long sourceAccountId, Long destAccountId, BigDecimal amount, String description) {
        // 1. Validate Inputs
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        Account dest = accountRepository.findById(destAccountId)
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        // 2. CHECK BALANCE (Prevent Overdraft)
        BigDecimal currentBalance = accountService.getBalance(sourceAccountId);
        if (currentBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds. Available: " + currentBalance);
        }

        // 3. Create the Transaction Record (The Intent)
        Transaction transaction = new Transaction();
        transaction.setType("TRANSFER");
        transaction.setSourceAccountId(sourceAccountId);
        transaction.setDestinationAccountId(destAccountId);
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setDescription(description);
        // Note: createdAt is handled by Database, but for safety in JPA:
        // transaction.setCreatedAt(LocalDateTime.now()); 
        
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 4. Create Ledger Entry 1: DEBIT Source
        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setTransaction(savedTransaction);
        debitEntry.setAccount(source);
        debitEntry.setType("DEBIT"); // Money leaving
        debitEntry.setAmount(amount);
        ledgerRepository.save(debitEntry);

        // 5. Create Ledger Entry 2: CREDIT Destination
        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setTransaction(savedTransaction);
        creditEntry.setAccount(dest);
        creditEntry.setType("CREDIT"); // Money entering
        creditEntry.setAmount(amount);
        ledgerRepository.save(creditEntry);

        return savedTransaction;
    }

    @Transactional
    public Transaction performDeposit(Long accountId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // 1. Transaction Record
        Transaction transaction = new Transaction();
        transaction.setType("DEPOSIT");
        transaction.setDestinationAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setDescription(description);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 2. Ledger Entry (Only Credit for deposit)
        LedgerEntry entry = new LedgerEntry();
        entry.setTransaction(savedTransaction);
        entry.setAccount(account);
        entry.setType("CREDIT");
        entry.setAmount(amount);
        ledgerRepository.save(entry);

        return savedTransaction;
    }
    
    // Note: You can add performWithdrawal similarly if needed
}