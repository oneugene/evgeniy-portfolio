package com.epam.ievgenii_onyshchenko.stockex.model;

public class BuyInput implements Input {
    private final int price;

    private final int amount;

    public BuyInput(int price, int amount) {
        this.price = price;
        this.amount = amount;
    }

    public int getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }
}
