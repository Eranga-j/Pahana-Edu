package com.pahanaedu.service.model;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;

@JsonbPropertyOrder({ "id", "itemId", "itemName", "qty", "unitPrice", "lineTotal" })
public class BillItem {

    private int id;
    private int itemId;
    private String itemName;

    /** Always ≥ 1 (default 1). */
    private int qty = 1;

    /** Never null; never negative. */
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** Never null; usually = qty * unitPrice. */
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public BillItem() { }

    public BillItem(int id, int itemId, String itemName, int qty,
                    BigDecimal unitPrice, BigDecimal lineTotal) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        setQty(qty);
        setUnitPrice(unitPrice);
        // If caller didn't pass lineTotal, compute it
        if (lineTotal == null) {
            recomputeLineTotal();
        } else {
            setLineTotal(lineTotal);
        }
    }

    // ---- Getters / Setters (null-safe & validated)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int v) { this.itemId = v; }

    public String getItemName() { return itemName; }
    public void setItemName(String v) { this.itemName = v; }

    public int getQty() { return qty; }
    public void setQty(int v) { this.qty = (v < 1) ? 1 : v; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal v) {
        if (v == null || v.signum() < 0) {
            this.unitPrice = BigDecimal.ZERO;
        } else {
            this.unitPrice = v;
        }
    }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal v) {
        if (v == null || v.signum() < 0) {
            // keep consistent rather than accept a negative/null
            recomputeLineTotal();
        } else {
            this.lineTotal = v;
        }
    }

    // ---- Helpers

    /** Recompute line total from qty * unitPrice (use before persist if needed). */
    public void recomputeLineTotal() {
        this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(this.qty));
    }

    /** Ensure lineTotal matches qty * unitPrice (useful after deserialization). */
    public void ensureConsistent() {
        recomputeLineTotal();
    }

    @Override
    public String toString() {
        return "BillItem{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", qty=" + qty +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + lineTotal +
                '}';
    }
}
