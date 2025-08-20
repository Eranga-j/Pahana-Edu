/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.pahanaedu.service.dao;

import com.pahanaedu.service.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author User
 */
public class UserDAOTest {
    
    public UserDAOTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validateLogin method, of class UserDAO.
     */
    @Test
    public void testValidateLogin() throws Exception {
        System.out.println("validateLogin");
        String username = "";
        String passwordHash = "";
        UserDAO instance = new UserDAO();
        User expResult = null;
        User result = instance.validateLogin(username, passwordHash);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSaltForUser method, of class UserDAO.
     */
    @Test
    public void testGetSaltForUser() throws Exception {
        System.out.println("getSaltForUser");
        String username = "";
        UserDAO instance = new UserDAO();
        String expResult = "";
        String result = instance.getSaltForUser(username);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
