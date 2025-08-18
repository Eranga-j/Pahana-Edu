package com.pahanaedu.service.dao;
import com.pahanaedu.service.db.Database; import com.pahanaedu.service.model.Customer; import java.sql.*; import java.util.*;
public class CustomerDAO {
    public List<Customer> findAll() throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT id, account_number, name, address, phone FROM customers ORDER BY id DESC"); ResultSet rs = ps.executeQuery()) {
            List<Customer> list = new ArrayList<>(); while (rs.next()) list.add(map(rs)); return list;
        }
    }
    public Customer findById(int id) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT id, account_number, name, address, phone FROM customers WHERE id=?")) {
            ps.setInt(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }
    public Customer create(Customer c) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO customers (account_number, name, address, phone) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getAccountNumber()); ps.setString(2, c.getName()); ps.setString(3, c.getAddress()); ps.setString(4, c.getPhone()); ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) c.setId(k.getInt(1)); } return c;
        }
    }
    public boolean update(int id, Customer c) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE customers SET account_number=?, name=?, address=?, phone=? WHERE id=?")) {
            ps.setString(1, c.getAccountNumber()); ps.setString(2, c.getName()); ps.setString(3, c.getAddress()); ps.setString(4, c.getPhone()); ps.setInt(5, id); return ps.executeUpdate()==1;
        }
    }
    public boolean delete(int id) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM customers WHERE id=?")) { ps.setInt(1, id); return ps.executeUpdate()==1; }
    }
    private Customer map(ResultSet rs) throws SQLException { return new Customer(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)); }
}
