package com.github.yuqingliu.economy.persistence.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
}
