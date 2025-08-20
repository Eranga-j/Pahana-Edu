/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.pahanaedu.service.dao;

import java.time.LocalDate;
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
public class ReportDAOTest {
    
    public ReportDAOTest() {
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
     * Test of getSalesSummary method, of class ReportDAO.
     */
    @Test
    public void testGetSalesSummary() throws Exception {
        System.out.println("getSalesSummary");
        LocalDate from = null;
        LocalDate to = null;
        ReportDAO instance = new ReportDAO();
        List<ReportDAO.SalesSummaryRow> expResult = null;
        List<ReportDAO.SalesSummaryRow> result = instance.getSalesSummary(from, to);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSalesByItem method, of class ReportDAO.
     */
    @Test
    public void testGetSalesByItem() throws Exception {
        System.out.println("getSalesByItem");
        LocalDate from = null;
        LocalDate to = null;
        ReportDAO instance = new ReportDAO();
        List<ReportDAO.ItemSalesRow> expResult = null;
        List<ReportDAO.ItemSalesRow> result = instance.getSalesByItem(from, to);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSalesByCustomer method, of class ReportDAO.
     */
    @Test
    public void testGetSalesByCustomer() throws Exception {
        System.out.println("getSalesByCustomer");
        LocalDate from = null;
        LocalDate to = null;
        ReportDAO instance = new ReportDAO();
        List<ReportDAO.CustomerSalesRow> expResult = null;
        List<ReportDAO.CustomerSalesRow> result = instance.getSalesByCustomer(from, to);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
