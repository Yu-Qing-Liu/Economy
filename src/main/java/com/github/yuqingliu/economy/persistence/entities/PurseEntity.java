package com.github.yuqingliu.economy.persistence.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurseEntity {
    @Id
    @Column(name = "playerId", columnDefinition = "VARCHAR(36)")
    private UUID playerId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "playerId", nullable = false)
    private PlayerEntity player;

    @OneToMany(mappedBy = "purse", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<CurrencyEntity> currencies = new HashSet<>();
}
