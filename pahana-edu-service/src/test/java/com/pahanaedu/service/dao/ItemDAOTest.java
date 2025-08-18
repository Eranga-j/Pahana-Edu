package com.pahanaedu.service.dao;

import com.pahanaedu.service.model.Item;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRUD tests for ItemDAO using an in-memory H2 database.
 * Requires: junit-jupiter + h2 (test scope) in pom.xml.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemDAOTest {

    private Connection conn;
    private ItemDAO dao;

    @BeforeAll
    void setupClass() throws Exception {
        // H2 with MySQL compatibility helps if your SQL uses MySQL features
        conn = DriverManager.getConnection(
                "jdbc:h2:mem:pahana;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");

        try (Statement s = conn.createStatement()) {
            // Align columns with your real schema. Adjust names if different.
            s.execute("""
                CREATE TABLE items (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(100) NOT NULL,
                  unit_price DECIMAL(10,2) NOT NULL
                );
            """);
        }

        // Inject the same connection into the DAO (constructor must exist)
        dao = new ItemDAO(() -> conn);
    }

    @AfterAll
    void tearDownClass() throws Exception {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    @BeforeEach
    void seed() throws Exception {
        try (Statement s = conn.createStatement()) {
            s.execute("DELETE FROM items");
            s.execute("ALTER TABLE items ALTER COLUMN id RESTART WITH 1");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO items(name, unit_price) VALUES (?, ?)")) {
                ps.setString(1, "Pencil");
                ps.setBigDecimal(2, new BigDecimal("50.00"));
                ps.executeUpdate();
            }
        }
    }

    @Test
    void findAll_shouldReturnAtLeastSeedItem() throws Exception {
        List<Item> items = dao.findAll();
        assertFalse(items.isEmpty(), "Should contain at least the seeded item");
        assertEquals("Pencil", items.get(0).getName());
    }

    @Test
    void findById_shouldReturnSeededItem() throws Exception {
        Item first = dao.findAll().get(0);
        Item found = dao.findById(first.getId());
        assertNotNull(found);
        assertEquals("Pencil", found.getName());
        assertEquals(new BigDecimal("50.00"), found.getUnitPrice());
    }

    @Test
    void create_shouldInsertAndReturnWithId() throws Exception {
        Item it = new Item();
        it.setName("Book");
        it.setUnitPrice(new BigDecimal("200.00"));

        Item saved = dao.create(it);
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);

        Item roundTrip = dao.findById(saved.getId());
        assertNotNull(roundTrip);
        assertEquals("Book", roundTrip.getName());
        assertEquals(new BigDecimal("200.00"), roundTrip.getUnitPrice());
    }

    @Test
    void update_shouldModifyNameAndPrice() throws Exception {
        // Arrange: create a new item to update
        Item it = new Item();
        it.setName("Notebook");
        it.setUnitPrice(new BigDecimal("120.00"));
        Item saved = dao.create(it);

        // Act
        saved.setName("Notebook A5");
        saved.setUnitPrice(new BigDecimal("150.00"));
        boolean ok = dao.update(saved.getId(), saved);

        // Assert
        assertTrue(ok, "update() should return true when a row is updated");
        Item updated = dao.findById(saved.getId());
        assertEquals("Notebook A5", updated.getName());
        assertEquals(new BigDecimal("150.00"), updated.getUnitPrice());
    }

    @Test
    void delete_shouldRemoveRow() throws Exception {
        Item it = new Item();
        it.setName("Eraser");
        it.setUnitPrice(new BigDecimal("30.00"));
        Item saved = dao.create(it);

        boolean ok = dao.delete(saved.getId());
        assertTrue(ok, "delete() should return true when a row is deleted");

        assertNull(dao.findById(saved.getId()), "Deleted row should not be found");
    }
}
