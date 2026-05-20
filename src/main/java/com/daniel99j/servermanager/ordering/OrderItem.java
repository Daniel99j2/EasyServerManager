package com.daniel99j.servermanager.ordering;

import com.daniel99j.servermanager.Item;

public class OrderItem {
    public final Item item;
    public final int quantity;

    public OrderItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
}
