package com.banking.ledger_system.repository;

import com.banking.ledger_system.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}