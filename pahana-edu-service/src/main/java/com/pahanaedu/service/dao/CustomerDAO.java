package com.pahanaedu.service.dao;

import com.pahanaedu.service.db.Database;
import com.pahanaedu.service.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // MySQL/MariaDB integrity violations use SQLState 23000 (e.g., duplicate key)
    public static final String SQLSTATE_INTEGRITY_VIOLATION = "23000";

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT id, account_number, name, address, phone " +
                     "FROM customers ORDER BY id DESC";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Customer> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT id, account_number, name, address, phone " +
                     "FROM customers WHERE id=?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Customer create(Customer c) throws SQLException {
        if (c == null) throw new IllegalArgumentException("Customer is null");
        String account = safe(c.getAccountNumber());
        String name    = safe(c.getName());
        String address = nullSafe(c.getAddress());
        String phone   = nullSafe(c.getPhone());

        if (account.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException("Account number and name are required");
        }

        String sql = "INSERT INTO customers (account_number, name, address, phone) VALUES (?,?,?,?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account);
            ps.setString(2, name);
            ps.setString(3, address);
            ps.setString(4, phone);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) c.setId(k.getInt(1));
            }
            // keep normalized values in returned object
            c.setAccountNumber(account);
            c.setName(name);
            c.setAddress(address);
            c.setPhone(phone);
            return c;

        } catch (SQLIntegrityConstraintViolationException dup) {
            // Re-throw the same type so the resource can translate to HTTP 409
            throw dup;
        }
    }

    public boolean update(int id, Customer c) throws SQLException {
        if (c == null) throw new IllegalArgumentException("Customer is null");
        String account = safe(c.getAccountNumber());
        String name    = safe(c.getName());
        String address = nullSafe(c.getAddress());
        String phone   = nullSafe(c.getPhone());

        String sql = "UPDATE customers SET account_number=?, name=?, address=?, phone=? WHERE id=?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, account);
            ps.setString(2, name);
            ps.setString(3, address);
            ps.setString(4, phone);
            ps.setInt(5, id);
            int updated = ps.executeUpdate();
            return updated == 1;

        } catch (SQLIntegrityConstraintViolationException dup) {
            // Duplicate account number on update
            throw dup;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id=?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    // ---------- helpers ----------

    private static Customer map(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt(1),       // id
            rs.getString(2),    // account_number
            rs.getString(3),    // name
            rs.getString(4),    // address
            rs.getString(5)     // phone
        );
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static String nullSafe(String s) { s = (s == null ? "" : s.trim()); return s.isEmpty() ? null : s; }
}
