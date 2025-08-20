package com.pahanaedu.service.dao;

import com.pahanaedu.service.db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ReportDAO {

    public static class SalesSummaryRow {
        public LocalDate day;
        public int invoices;
        public BigDecimal subtotal, discountAmount, taxAmount, totalAmount;
    }

    public static class ItemSalesRow {
        public int itemId;
        public String sku, name;
        public BigDecimal qtySold, salesAmount;
    }

    public static class CustomerSalesRow {
        public int customerId;
        public String customerName;
        public Timestamp firstPurchase, lastPurchase;
        public int invoices;
        public BigDecimal totalSpent;
    }

    private static java.sql.Date sql(LocalDate d) { return (d == null ? null : java.sql.Date.valueOf(d)); }

    public List<SalesSummaryRow> getSalesSummary(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT DATE(b.bill_date) AS day,
                   COUNT(DISTINCT b.id) AS invoices,
                   SUM(b.subtotal) AS subtotal,
                   SUM(b.discount_amount) AS discount_amount,
                   SUM(b.tax_amount) AS tax_amount,
                   SUM(b.total_amount) AS total_amount
            FROM bills b
            WHERE (? IS NULL OR DATE(b.bill_date) >= ?)
              AND (? IS NULL OR DATE(b.bill_date) <= ?)
            GROUP BY DATE(b.bill_date)
            ORDER BY day
        """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, sql(from)); ps.setDate(2, sql(from));
            ps.setDate(3, sql(to));   ps.setDate(4, sql(to));
            List<SalesSummaryRow> rows = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalesSummaryRow r = new SalesSummaryRow();
                    java.sql.Date d = rs.getDate("day");
                    r.day = (d == null ? null : d.toLocalDate());
                    r.invoices = rs.getInt("invoices");
                    r.subtotal = rs.getBigDecimal("subtotal");
                    r.discountAmount = rs.getBigDecimal("discount_amount");
                    r.taxAmount = rs.getBigDecimal("tax_amount");
                    r.totalAmount = rs.getBigDecimal("total_amount");
                    rows.add(r);
                }
            }
            return rows;
        }
    }

    public List<ItemSalesRow> getSalesByItem(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT i.id AS item_id, i.sku, i.name,
                   SUM(bi.quantity) AS qty_sold,
                   SUM(bi.quantity * bi.unit_price) AS sales_amount
            FROM bill_items bi
            JOIN items i ON i.id = bi.item_id
            JOIN bills b ON b.id = bi.bill_id
            WHERE (? IS NULL OR DATE(b.bill_date) >= ?)
              AND (? IS NULL OR DATE(b.bill_date) <= ?)
            GROUP BY i.id, i.sku, i.name
            ORDER BY sales_amount DESC
        """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, sql(from)); ps.setDate(2, sql(from));
            ps.setDate(3, sql(to));   ps.setDate(4, sql(to));
            List<ItemSalesRow> rows = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemSalesRow r = new ItemSalesRow();
                    r.itemId = rs.getInt("item_id");
                    r.sku = rs.getString("sku");
                    r.name = rs.getString("name");
                    r.qtySold = rs.getBigDecimal("qty_sold");
                    r.salesAmount = rs.getBigDecimal("sales_amount");
                    rows.add(r);
                }
            }
            return rows;
        }
    }

    public List<CustomerSalesRow> getSalesByCustomer(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT c.id AS customer_id, c.name AS customer_name,
                   MIN(b.bill_date) AS first_purchase,
                   MAX(b.bill_date) AS last_purchase,
                   COUNT(DISTINCT b.id) AS invoices,
                   SUM(b.total_amount) AS total_spent
            FROM bills b
            JOIN customers c ON c.id = b.customer_id
            WHERE (? IS NULL OR DATE(b.bill_date) >= ?)
              AND (? IS NULL OR DATE(b.bill_date) <= ?)
            GROUP BY c.id, c.name
            ORDER BY total_spent DESC
        """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, sql(from)); ps.setDate(2, sql(from));
            ps.setDate(3, sql(to));   ps.setDate(4, sql(to));
            List<CustomerSalesRow> rows = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomerSalesRow r = new CustomerSalesRow();
                    r.customerId = rs.getInt("customer_id");
                    r.customerName = rs.getString("customer_name");
                    r.firstPurchase = rs.getTimestamp("first_purchase");
                    r.lastPurchase = rs.getTimestamp("last_purchase");
                    r.invoices = rs.getInt("invoices");
                    r.totalSpent = rs.getBigDecimal("total_spent");
                    rows.add(r);
                }
            }
            return rows;
        }
    }
}