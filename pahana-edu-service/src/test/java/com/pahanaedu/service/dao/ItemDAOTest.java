package com.pahanaedu.service.dao;

import com.pahanaedu.service.model.Item;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDAOTest {

    private ItemDAO dao;

    @BeforeEach
    void setUp() {
        dao = new ItemDAO();
    }

    private static Item newItem() {
        Item it = new Item();
        it.setSku("T" + System.nanoTime());     // unique per run (sku is UNIQUE)
        it.setName("Test Item");
        it.setUnitPrice(new BigDecimal("123.45"));
        return it;
    }

    @Test
    void testFindAll() throws Exception {
        List<Item> list = dao.findAll();
        assertNotNull(list, "findAll should not return null");
        // size may be 0 on CI; that's fine
    }

    @Test
    void testCreateAndFindById() throws Exception {
        Item created = dao.create(newItem());
        assertNotNull(created, "create must return an Item");
        assertTrue(created.getId() > 0, "ID must be generated");
        assertNotNull(created.getSku(), "SKU should be set");

        Item fetched = dao.findById(created.getId());
        assertNotNull(fetched, "findById must return the created item");
        assertEquals(created.getSku(), fetched.getSku(), "SKU must match");

        // cleanup
        assertTrue(dao.delete(created.getId()), "cleanup delete should succeed");
    }

    @Test
    void testUpdate() throws Exception {
        Item created = dao.create(newItem());
        int id = created.getId();

        // change a couple of fields
        created.setName("Updated Name");
        created.setUnitPrice(new BigDecimal("199.99"));

        // Some DAOs use update(int, Item), some update(Item); handle both
        boolean ok = callUpdateReflective(dao, created);
        assertTrue(ok, "update should return true");

        Item fetched = dao.findById(id);
        assertNotNull(fetched, "updated item should exist");
        assertEquals("Updated Name", fetched.getName());
        assertEquals(0, new BigDecimal("199.99").compareTo(fetched.getUnitPrice()));

        // cleanup
        assertTrue(dao.delete(id), "cleanup delete should succeed");
    }

    @Test
    void testDelete() throws Exception {
        Item created = dao.create(newItem());
        int id = created.getId();

        assertTrue(dao.delete(id), "delete should return true");
        assertNull(dao.findById(id), "deleted item should not be found");
    }

    /** Try update(Item) first; if not present, try update(int, Item). */
    private static boolean callUpdateReflective(ItemDAO dao, Item it) throws Exception {
        try {
            Method m = ItemDAO.class.getMethod("update", Item.class);
            Object r = m.invoke(dao, it);
            return (r instanceof Boolean) ? (Boolean) r : true;
        } catch (NoSuchMethodException e) {
            Method m = ItemDAO.class.getMethod("update", int.class, Item.class);
            Object r = m.invoke(dao, it.getId(), it);
            return (r instanceof Boolean) ? (Boolean) r : true;
        }
    }
}
