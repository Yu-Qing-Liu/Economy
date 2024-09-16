package com.github.yuqingliu.economy.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "banks")
@Getter
@Setter
@RequiredArgsConstructor
public class BankEntity {
    @Id
    @Column(name = "bankName", columnDefinition = "VARCHAR(16)")
    private final String name;

    @OneToMany(mappedBy = "bank")
    private Set<AccountEntity> accounts;
}
