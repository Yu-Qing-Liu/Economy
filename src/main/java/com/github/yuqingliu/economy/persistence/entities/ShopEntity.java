package com.github.yuqingliu.economy.persistence.entities;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
public class ShopEntity {
    @Id
    @Column(name = "shopName", columnDefinition = "VARCHAR(16)")
    private String shopName;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ShopSectionEntity> sections = new LinkedHashSet<>();
}
