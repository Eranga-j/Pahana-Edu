/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.pahanaedu.service.dao;

import com.pahanaedu.service.model.Bill;
import java.sql.Timestamp;
import java.util.List;
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
public class BillDAOTest {
    
    public BillDAOTest() {
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
     * Test of create method, of class BillDAO.
     */
    @Test
    public void testCreate() throws Exception {
        System.out.println("create");
        Bill bill = null;
        BillDAO instance = new BillDAO();
        Bill expResult = null;
        Bill result = instance.create(bill);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findById method, of class BillDAO.
     */
    @Test
    public void testFindById() throws Exception {
        System.out.println("findById");
        int id = 0;
        BillDAO instance = new BillDAO();
        Bill expResult = null;
        Bill result = instance.findById(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of listRange method, of class BillDAO.
     */
    @Test
    public void testListRange() throws Exception {
        System.out.println("listRange");
        Timestamp from = null;
        Timestamp to = null;
        BillDAO instance = new BillDAO();
        List<Bill> expResult = null;
        List<Bill> result = instance.listRange(from, to);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
