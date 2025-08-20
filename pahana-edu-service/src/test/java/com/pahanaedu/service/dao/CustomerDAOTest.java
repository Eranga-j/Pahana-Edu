/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.pahanaedu.service.dao;

import com.pahanaedu.service.model.Customer;
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
public class CustomerDAOTest {
    
    public CustomerDAOTest() {
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
     * Test of findAll method, of class CustomerDAO.
     */
    @Test
    public void testFindAll() throws Exception {
        System.out.println("findAll");
        CustomerDAO instance = new CustomerDAO();
        List<Customer> expResult = null;
        List<Customer> result = instance.findAll();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findById method, of class CustomerDAO.
     */
    @Test
    public void testFindById() throws Exception {
        System.out.println("findById");
        int id = 0;
        CustomerDAO instance = new CustomerDAO();
        Customer expResult = null;
        Customer result = instance.findById(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of create method, of class CustomerDAO.
     */
    @Test
    public void testCreate() throws Exception {
        System.out.println("create");
        Customer c = null;
        CustomerDAO instance = new CustomerDAO();
        Customer expResult = null;
        Customer result = instance.create(c);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class CustomerDAO.
     */
    @Test
    public void testUpdate() throws Exception {
        System.out.println("update");
        int id = 0;
        Customer c = null;
        CustomerDAO instance = new CustomerDAO();
        boolean expResult = false;
        boolean result = instance.update(id, c);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class CustomerDAO.
     */
    @Test
    public void testDelete() throws Exception {
        System.out.println("delete");
        int id = 0;
        CustomerDAO instance = new CustomerDAO();
        boolean expResult = false;
        boolean result = instance.delete(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
