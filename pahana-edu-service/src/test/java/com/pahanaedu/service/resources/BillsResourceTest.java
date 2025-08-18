/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.pahanaedu.service.resources;

import jakarta.ws.rs.core.Response;
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
public class BillsResourceTest {
    
    public BillsResourceTest() {
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
     * Test of create method, of class BillsResource.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        BillsResource.CreateBillRequest req = null;
        BillsResource instance = new BillsResource();
        Response expResult = null;
        Response result = instance.create(req);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get method, of class BillsResource.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        int id = 0;
        BillsResource instance = new BillsResource();
        Response expResult = null;
        Response result = instance.get(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of list method, of class BillsResource.
     */
    @Test
    public void testList() {
        System.out.println("list");
        String from = "";
        String to = "";
        BillsResource instance = new BillsResource();
        Response expResult = null;
        Response result = instance.list(from, to);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
