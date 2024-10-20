package com.github.yuqingliu.economy.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany(mappedBy = "bank")
    private Set<AccountEntity> accounts = new LinkedHashSet<>();
}
