package com.github.yuqingliu.economy.persistence.entities.keys;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class VendorItemKey implements Serializable {
    private String itemName;
    private String sectionName;
    private String vendorName;
}
