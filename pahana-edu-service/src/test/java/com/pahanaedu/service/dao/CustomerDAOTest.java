package com.pahanaedu.service.dao;

import com.pahanaedu.service.model.Customer;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerDAOTest {

    private CustomerDAO dao;

    @BeforeEach
    void setUp() {
        dao = new CustomerDAO();
    }

    private static Customer newCustomer() {
        Customer c = new Customer();
        c.setAccountNumber("T" + System.nanoTime()); // unique per test run
        c.setName("Test Customer");
        c.setAddress("Test Address");
        c.setPhone("0712345678");
        return c;
    }

    @Test
    void testFindAll() throws Exception {
        List<Customer> list = dao.findAll();
        assertNotNull(list, "findAll should not return null");
        // list may be empty on first run — that's OK. No size assertion here.
    }

    @Test
    void testCreateAndFindById() throws Exception {
        Customer created = dao.create(newCustomer());
        assertNotNull(created, "create must return a Customer");
        assertTrue(created.getId() > 0, "ID must be generated");
        assertNotNull(created.getAccountNumber(), "accountNumber should be set");

        Customer fetched = dao.findById(created.getId());
        assertNotNull(fetched, "findById must return the created customer");
        assertEquals(created.getAccountNumber(), fetched.getAccountNumber(), "accountNumber must match");

        // cleanup
        assertTrue(dao.delete(created.getId()), "cleanup delete should succeed");
    }

    @Test
    void testUpdate() throws Exception {
        Customer created = dao.create(newCustomer());
        int id = created.getId();

        // mutate fields
        created.setName("Updated Name");
        created.setPhone("0770000000");

        // Your DAO signature is update(int, Customer) per the prototype test
        boolean updated = dao.update(id, created);
        assertTrue(updated, "update should return true");

        Customer fetched = dao.findById(id);
        assertNotNull(fetched, "updated customer should exist");
        assertEquals("Updated Name", fetched.getName());
        assertEquals("0770000000", fetched.getPhone());

        // cleanup
        assertTrue(dao.delete(id), "cleanup delete should succeed");
    }

    @Test
    void testDelete() throws Exception {
        Customer created = dao.create(newCustomer());
        int id = created.getId();

        assertTrue(dao.delete(id), "delete should return true");
        assertNull(dao.findById(id), "deleted customer should not be found");
    }
}
