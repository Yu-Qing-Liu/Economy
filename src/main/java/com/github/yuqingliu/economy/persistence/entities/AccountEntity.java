package com.github.yuqingliu.economy.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountEntity {
    @Id
    @Column(name = "accountId", columnDefinition = "VARCHAR(36)")
    private UUID accountId;

    @ManyToOne
    @JoinColumn(name = "bankName", nullable = false)
    private BankEntity bank;

    @ManyToOne
    @JoinColumn(name = "playerId", nullable = false)
    private PlayerEntity player;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<CurrencyEntity> currencies = new LinkedHashSet<>();
}

