package com.github.yuqingliu.economy.persistence.entities;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class PlayerEntity {
    @Id
    @Column(name = "playerId", columnDefinition = "VARCHAR(36)")
    private UUID playerId;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private PurseEntity purse;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountEntity> accounts = new LinkedHashSet<>();

    public void setPurse(PurseEntity purse) {
        if (purse != null) {
            purse.setPlayer(this); // Ensure bidirectional consistency
        }
        this.purse = purse;
    }
}
