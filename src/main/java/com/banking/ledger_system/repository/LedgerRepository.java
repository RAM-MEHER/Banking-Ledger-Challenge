package com.banking.ledger_system.repository;

import com.banking.ledger_system.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    // This custom method finds all ledger entries for a specific account
    List<LedgerEntry> findByAccountId(Long accountId);
}