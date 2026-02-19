package com.example.vendingmachine.model;

import lombok.Getter;

@Getter
public enum Coin {
    PENNY(0.01),
    NICKEL(0.05),
    DIME(0.10),
    QUARTER(0.25);

    private final double value;

    Coin(double value) {
        this.value = value;
    }
}
