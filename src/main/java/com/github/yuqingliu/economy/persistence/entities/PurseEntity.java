package com.github.yuqingliu.economy.persistence.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
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

    public PurseEntity(UUID playerId) {
        this.playerId = playerId;
    }
}
