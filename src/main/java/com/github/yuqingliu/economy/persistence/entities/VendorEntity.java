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
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
public class VendorEntity {
    @Id
    @Column(name = "vendorName", columnDefinition = "VARCHAR(16)")
    private String vendorName;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VendorSectionEntity> sections = new LinkedHashSet<>();
}
