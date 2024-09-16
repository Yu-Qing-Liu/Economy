package com.github.yuqingliu.economy.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@RequiredArgsConstructor
public class AccountEntity {
    @Id
    @Column(name = "accountId", columnDefinition = "VARCHAR(36)")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "bankName", nullable = false)
    private BankEntity bank;

    @ManyToOne
    @JoinColumn(name = "playerId", nullable = false)
    private PlayerEntity player;
}

