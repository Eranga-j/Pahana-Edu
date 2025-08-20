package com.pahanaedu.service.dao;

import com.pahanaedu.service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private UserDAO dao;

    @BeforeEach
    void setUp() {
        dao = new UserDAO();
    }

    private static String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(dig.length * 2);
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Test
    public void testValidateLogin_success_and_failure() throws Exception {
        final String username = "admin";        // seeded in pahana_edu_new_database.sql
        final String plainPwd = "admin123";     // seeded password

        // 1) Get salt and compute hash(password + salt)
        String salt = dao.getSaltForUser(username);
        assertNotNull(salt, "Salt should exist for seeded admin user");
        assertFalse(salt.isEmpty(), "Salt should not be empty");

        String goodHash = sha256(plainPwd + salt);

        // 2) Success
        User u = dao.validateLogin(username, goodHash);
        assertNotNull(u, "validateLogin should return a User for correct credentials");

        // 3) Failure with wrong hash
        String badHash = sha256("wrong" + salt);
        User uBad = dao.validateLogin(username, badHash);
        assertNull(uBad, "validateLogin should return null for wrong credentials");
    }

    @Test
    public void testGetSaltForUser() throws Exception {
        // Existing user
        String salt = dao.getSaltForUser("admin");
        assertNotNull(salt);
        assertFalse(salt.isEmpty());

        // Non-existent user: allow DAO to return null OR empty string
        String missing = dao.getSaltForUser("no_such_user_xyz");
        assertTrue(missing == null || missing.isEmpty(),
                "Expected null or empty salt for missing user");
    }
}
