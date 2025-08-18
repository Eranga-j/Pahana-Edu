package com.pahanaedu.service.dao;
import com.pahanaedu.service.db.Database; import com.pahanaedu.service.model.*; import java.sql.*; import java.util.*; import java.time.LocalDateTime;
public class BillDAO {
    public Bill create(Bill bill) throws SQLException {
        String billSql = "INSERT INTO bills (bill_no, customer_id, total_amount, created_by) VALUES (?,?,?,?)";
        String lineSql = "INSERT INTO bill_items (bill_id, item_id, qty, unit_price, line_total) VALUES (?,?,?,?,?)";
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, bill.getBillNo()); ps.setInt(2, bill.getCustomerId()); ps.setBigDecimal(3, bill.getTotalAmount()); ps.setInt(4, bill.getCreatedBy());
                ps.executeUpdate();
                int billId; try (ResultSet keys = ps.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("No bill id generated"); billId = keys.getInt(1); }
                for (BillItem bi: bill.getItems()) try (PreparedStatement line = con.prepareStatement(lineSql)) {
                    line.setInt(1, billId); line.setInt(2, bi.getItemId()); line.setInt(3, bi.getQty()); line.setBigDecimal(4, bi.getUnitPrice()); line.setBigDecimal(5, bi.getLineTotal()); line.executeUpdate();
                }
                con.commit(); return findById(billId);
            } catch (SQLException e) { con.rollback(); throw e; } finally { con.setAutoCommit(true); }
        }
    }
    public Bill findById(int id) throws SQLException {
        String billSql = "SELECT b.id,b.bill_no,b.customer_id,c.name,b.total_amount,b.created_at,b.created_by FROM bills b JOIN customers c ON b.customer_id=c.id WHERE b.id=?";
        String itemsSql = "SELECT bi.id, bi.item_id, i.name, bi.qty, bi.unit_price, bi.line_total FROM bill_items bi JOIN items i ON bi.item_id=i.id WHERE bi.bill_id=?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(billSql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Bill bill = new Bill();
                bill.setId(rs.getInt(1)); bill.setBillNo(rs.getString(2)); bill.setCustomerId(rs.getInt(3)); bill.setCustomerName(rs.getString(4));
                bill.setTotalAmount(rs.getBigDecimal(5)); java.sql.Timestamp ts = rs.getTimestamp(6); bill.setCreatedAt(ts!=null? ts.toLocalDateTime(): LocalDateTime.now()); bill.setCreatedBy(rs.getInt(7));
                java.util.List<BillItem> items = new java.util.ArrayList<>();
                try (PreparedStatement ip = con.prepareStatement(itemsSql)) {
                    ip.setInt(1, id); try (ResultSet irs = ip.executeQuery()) {
                        while (irs.next()) items.add(new BillItem(irs.getInt(1), irs.getInt(2), irs.getString(3), irs.getInt(4), irs.getBigDecimal(5), irs.getBigDecimal(6)));
                    }
                }
                bill.setItems(items); return bill;
            }
        }
    }
    public java.util.List<Bill> listRange(java.sql.Timestamp from, java.sql.Timestamp to) throws SQLException {
        String billSql = "SELECT b.id,b.bill_no,b.customer_id,c.name,b.total_amount,b.created_at,b.created_by FROM bills b JOIN customers c ON b.customer_id=c.id WHERE b.created_at BETWEEN ? AND ? ORDER BY b.created_at DESC";
        java.util.List<Bill> list = new java.util.ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(billSql)) {
            ps.setTimestamp(1, from); ps.setTimestamp(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { Bill b = new Bill(); b.setId(rs.getInt(1)); b.setBillNo(rs.getString(2)); b.setCustomerId(rs.getInt(3)); b.setCustomerName(rs.getString(4)); b.setTotalAmount(rs.getBigDecimal(5)); java.sql.Timestamp ts=rs.getTimestamp(6); b.setCreatedAt(ts!=null? ts.toLocalDateTime(): null); b.setCreatedBy(rs.getInt(7)); b.setItems(java.util.Collections.emptyList()); list.add(b); }
            }
        }
        return list;
        }
}
