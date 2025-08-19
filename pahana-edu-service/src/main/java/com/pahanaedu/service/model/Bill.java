package com.pahanaedu.service.model;

import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonbPropertyOrder({
        "id", "billNo", "customerId", "customerName",
        "totalAmount", "createdAt", "createdBy", "items"
})
public class Bill {

    private int id;
    private String billNo;
    private int customerId;
    private String customerName;

    /** Always non-null; defaults to ZERO. */
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** Serialize as ISO datetime (e.g., 2025-08-19T12:34:56). */
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    private int createdBy;

    /** Always non-null; defaults to empty list. */
    private List<BillItem> items = new ArrayList<>();

    public Bill() { }

    public Bill(int id,
                String billNo,
                int customerId,
                String customerName,
                BigDecimal totalAmount,
                LocalDateTime createdAt,
                int createdBy,
                List<BillItem> items) {
        this.id = id;
        this.billNo = billNo;
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = (totalAmount != null) ? totalAmount : BigDecimal.ZERO;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.createdBy = createdBy;
        if (items != null) this.items = new ArrayList<>(items);
    }

    // -------- Getters / Setters (null-safe where needed)

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }

    public String getBillNo() { return billNo; }
    public void setBillNo(String v) { this.billNo = v; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int v) { this.customerId = v; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { this.customerName = v; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal v) { this.totalAmount = (v != null) ? v : BigDecimal.ZERO; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = (v != null) ? v : LocalDateTime.now(); }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int v) { this.createdBy = v; }

    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> v) {
        this.items = (v != null) ? new ArrayList<>(v) : new ArrayList<>();
    }

    // -------- Convenience helpers

    /** Add a single item (null-safe) and keep totals consistent. */
    public void addItem(BillItem item) {
        if (item == null) return;
        if (this.items == null) this.items = new ArrayList<>();
        this.items.add(item);
        // If lineTotal missing, compute it from qty * unitPrice
        if (item.getLineTotal() == null) {
            BigDecimal price = (item.getUnitPrice() != null) ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal qty = BigDecimal.valueOf(item.getQty());
            item.setLineTotal(price.multiply(qty));
        }
        // Update the bill total
        this.totalAmount = (this.totalAmount != null ? this.totalAmount : BigDecimal.ZERO)
                .add(item.getLineTotal() != null ? item.getLineTotal() : BigDecimal.ZERO);
    }

    /** Recompute totalAmount by summing item line totals (useful before persisting). */
    public void recomputeTotals() {
        BigDecimal sum = BigDecimal.ZERO;
        if (items != null) {
            for (BillItem it : items) {
                if (it == null) continue;
                BigDecimal lt = it.getLineTotal();
                if (lt == null) {
                    BigDecimal price = (it.getUnitPrice() != null) ? it.getUnitPrice() : BigDecimal.ZERO;
                    lt = price.multiply(BigDecimal.valueOf(it.getQty()));
                    it.setLineTotal(lt);
                }
                sum = sum.add(lt);
            }
        }
        this.totalAmount = sum;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", billNo='" + billNo + '\'' +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                ", items=" + (items != null ? items.size() : 0) +
                '}';
    }
}
