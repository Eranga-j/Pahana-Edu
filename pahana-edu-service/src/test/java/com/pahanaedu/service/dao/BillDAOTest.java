package com.pahanaedu.service.dao;

import com.pahanaedu.service.model.Bill;
import com.pahanaedu.service.model.BillItem;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BillDAOTest {

    private BillDAO dao;

    @BeforeEach
    void setUp() {
        dao = new BillDAO();
    }

    @Test
    void testCreate() throws Exception {
        // Build a minimal valid bill using seeded data:
        // customer_id = 1 (Walk-in), item_id = 1 must exist
        Bill b = new Bill();
        b.setBillNo("INV-TEST-" + System.currentTimeMillis());
        b.setCustomerId(1);
        b.setCreatedBy(1);

        BillItem line = new BillItem();
        line.setItemId(1);
        line.setQty(2);
        line.setUnitPrice(new BigDecimal("250.00"));
        line.setLineTotal(new BigDecimal("500.00"));
        b.setItems(List.of(line));

        // totalAmount is required by BillDAO insert; triggers will also recompute
        b.setTotalAmount(new BigDecimal("500.00"));

        Bill created = dao.create(b);
        assertNotNull(created, "Bill should be returned");
        assertTrue(created.getId() > 0, "Bill ID should be generated");
        assertNotNull(created.getBillNo(), "BillNo should not be null");
    }

    @Test
    void testFindById() throws Exception {
        // Create one, then fetch by id
        Bill b = new Bill();
        b.setBillNo("INV-TEST-FIND-" + System.currentTimeMillis());
        b.setCustomerId(1);
        b.setCreatedBy(1);

        BillItem line = new BillItem();
        line.setItemId(1);
        line.setQty(1);
        line.setUnitPrice(new BigDecimal("65.00"));
        line.setLineTotal(new BigDecimal("65.00"));
        b.setItems(List.of(line));
        b.setTotalAmount(new BigDecimal("65.00"));

        Bill created = dao.create(b);
        assertNotNull(created);
        int id = created.getId();

        Bill found = dao.findById(id);
        assertNotNull(found, "findById should return the bill we created");
        assertEquals(created.getBillNo(), found.getBillNo(), "BillNo must match");
        assertEquals(1, found.getItems().size(), "One line expected");
    }

    @Test
    void testListRange() throws Exception {
        // Query recent period; listRange should never return null (empty list is OK)
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to   = LocalDateTime.now().plusMinutes(1);

        List<Bill> list = dao.listRange(Timestamp.valueOf(from), Timestamp.valueOf(to));
        assertNotNull(list, "listRange should not return null");
        // We don't assert size, because DB might be empty in CI; empty list is acceptable
    }
}
