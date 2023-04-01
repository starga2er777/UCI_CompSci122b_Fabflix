package org.uci.dto;

public class ItemInfo {
    public String title;
    public int amount;
    public float price;

    public ItemInfo(String title, int amount, float price) {
        this.title = title;
        this.amount = amount;
        this.price = price;
    }
}
