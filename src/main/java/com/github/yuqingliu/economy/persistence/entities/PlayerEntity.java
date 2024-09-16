package com.github.yuqingliu.economy.persistence.entities;

import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "players")
@Getter
@Setter
@RequiredArgsConstructor
public class PlayerEntity {
    @Id
    @Column(name = "playerId", columnDefinition = "VARCHAR(36)")
    private final UUID id;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private PurseEntity purse;

    public void setPurse(PurseEntity purse) {
        if (purse != null) {
            purse.setPlayer(this); // Ensure bidirectional consistency
        }
        this.purse = purse;
    }
}
