package com.banking.ledger_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String type; 

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String status; 

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}