package com.github.yuqingliu.economy.persistence.entities.keys;

import java.io.Serializable;
import java.util.UUID;

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
public class CurrencyKey implements Serializable {
    private String currencyName;
    private UUID accountId;
    private UUID purseId;
}
