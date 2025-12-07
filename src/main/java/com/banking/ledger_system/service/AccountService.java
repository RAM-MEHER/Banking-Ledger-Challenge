package com.banking.ledger_system.service;

import com.banking.ledger_system.model.Account;
import com.banking.ledger_system.model.LedgerEntry;
import com.banking.ledger_system.repository.AccountRepository;
import com.banking.ledger_system.repository.LedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;

    public AccountService(AccountRepository accountRepository, LedgerRepository ledgerRepository) {
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
    }

    // 1. Create a new Account
    public Account createAccount(Account account) {
        // Set defaults if missing
        if (account.getCurrency() == null) account.setCurrency("USD");
        if (account.getStatus() == null) account.setStatus("ACTIVE");
        return accountRepository.save(account);
    }

    // 2. Get Account (and calculate balance on the fly!)
    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    // 3. THE CORE LOGIC: Calculate Balance from Ledger History
    public BigDecimal getBalance(Long accountId) {
        List<LedgerEntry> entries = ledgerRepository.findByAccountId(accountId);
        
        BigDecimal balance = BigDecimal.ZERO;
        
        for (LedgerEntry entry : entries) {
            if ("CREDIT".equals(entry.getType())) {
                balance = balance.add(entry.getAmount());
            } else if ("DEBIT".equals(entry.getType())) {
                balance = balance.subtract(entry.getAmount());
            }
        }
        
        return balance;
    }
}