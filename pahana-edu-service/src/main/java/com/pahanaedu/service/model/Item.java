package com.pahanaedu.service.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Item {

    private int id;
    private String sku;
    private String name;
    private BigDecimal unitPrice;

    public Item() {
    }

    public Item(int id, String sku, String name, BigDecimal unitPrice) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.unitPrice = unitPrice;
    }

    // Convenience constructor (no id yet, e.g. before persisting)
    public Item(String sku, String name, BigDecimal unitPrice) {
        this(0, sku, name, unitPrice);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }
    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", unitPrice=" + unitPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return id == item.id &&
                Objects.equals(sku, item.sku) &&
                Objects.equals(name, item.name) &&
                Objects.equals(unitPrice, item.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sku, name, unitPrice);
    }
}
