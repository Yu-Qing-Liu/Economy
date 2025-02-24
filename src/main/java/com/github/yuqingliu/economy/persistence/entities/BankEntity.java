package com.github.yuqingliu.economy.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "banks")
@Getter
@Setter
@NoArgsConstructor
public class BankEntity {
    @Id
    @Column(name = "bankName", columnDefinition = "VARCHAR(16)")
    private String bankName;

    @Column(name = "interestCooldown")
    private long interestCooldown;

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("accountName ASC")
    private Set<AccountEntity> accounts = new LinkedHashSet<>();

    public Duration getInterestCooldown() {
        return Duration.ofHours(interestCooldown);
    }
}
