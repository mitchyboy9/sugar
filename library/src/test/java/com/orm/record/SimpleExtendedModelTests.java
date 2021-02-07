package com.orm.record;

import com.orm.app.ClientApp;
import com.orm.helper.NamingHelper;
import com.orm.model.SimpleExtendedModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.orm.SugarRecord.save;
import static com.orm.SugarRecord.count;
import static com.orm.SugarRecord.delete;
import static com.orm.SugarRecord.deleteAll;
import static com.orm.SugarRecord.executeQuery;
import static com.orm.SugarRecord.find;
import static com.orm.SugarRecord.findAll;
import static com.orm.SugarRecord.findById;
import static com.orm.SugarRecord.findWithQuery;
import static com.orm.SugarRecord.findAsIterator;
import static com.orm.SugarRecord.findWithQueryAsIterator;
import static com.orm.SugarRecord.deleteInTx;
import static com.orm.SugarRecord.saveInTx;
import static com.orm.SugarRecord.listAll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18, application = ClientApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public final class SimpleExtendedModelTests {
    private String id = "id = ?";

    @Test
    public void emptyDatabaseTest() {
        assertEquals(0L, count(SimpleExtendedModel.class));
    }

    @Test
    public void oneSaveTest() {
        save(new SimpleExtendedModel());
        assertEquals(1L, count(SimpleExtendedModel.class));
    }

    @Test
    public void twoSaveTest() {
        save(new SimpleExtendedModel());
        save(new SimpleExtendedModel());
        assertEquals(2L, count(SimpleExtendedModel.class));
    }

    @Test
    public void manySaveTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleExtendedModel());
        }

        assertEquals(100L, count(SimpleExtendedModel.class));
    }

    @Test
    public void defaultIdTest() {
        assertEquals(1L, save(new SimpleExtendedModel()));
    }

    @Test
    public void whereCountTest() {
        save(new SimpleExtendedModel());
        save(new SimpleExtendedModel());
        assertEquals(1L, count(SimpleExtendedModel.class, id, "1"));
    }

    @Test
    public void whereNoCountTest() {
        assertEquals(0L, count(SimpleExtendedModel.class, id, "1"));
        save(new SimpleExtendedModel());
        save(new SimpleExtendedModel());
        assertEquals(0L, count(SimpleExtendedModel.class, id, "3"));
        assertEquals(0L, count(SimpleExtendedModel.class, id, "a"));
    }

    @Test
    public void whereBrokenCountTest() {
        save(new SimpleExtendedModel());
        save(new SimpleExtendedModel());
        assertEquals(-1L, count(SimpleExtendedModel.class, "di = ?", "1"));
    }

    @Test
    public void saveMethodTest() {
        SimpleExtendedModel model = new SimpleExtendedModel();
        model.save();
        assertEquals(-1L, count(SimpleExtendedModel.class, "di = ?", "1"));
    }

    @Test
    public void deleteTest() {
        SimpleExtendedModel model = new SimpleExtendedModel();
        save(model);
        assertEquals(1L, count(SimpleExtendedModel.class));
        assertTrue(delete(model));
        assertEquals(0L, count(SimpleExtendedModel.class));
    }

    @Test
    public void deleteUnsavedTest() {
        SimpleExtendedModel model = new SimpleExtendedModel();
        assertEquals(0L, count(SimpleExtendedModel.class));
        assertFalse(delete(model));
        assertEquals(0L, count(SimpleExtendedModel.class));
    }

    @Test
    public void deleteWrongTest() throws Exception {
        SimpleExtendedModel model = new SimpleExtendedModel();
        save(model);
        assertEquals(1L, count(SimpleExtendedModel.class));
        Field idField = model.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(model, Long.MAX_VALUE);
        assertFalse(delete(model));
        assertEquals(1L, count(SimpleExtendedModel.class));
    }

    @Test
    public void deleteAllTest() {
        int elementNumber = 100;
        for (int i = 1; i <= elementNumber; i++) {
            save(new SimpleExtendedModel());
        }
        assertEquals(elementNumber, deleteAll(SimpleExtendedModel.class));
        assertEquals(0L, count(SimpleExtendedModel.class));
    }

    @Test
    @SuppressWarnings("all")
    public void deleteAllWhereTest() {
        int elementNumber = 100;
        for (int i = 1; i <= elementNumber; i++) {
            save(new SimpleExtendedModel());
        }
        assertEquals(elementNumber - 1, deleteAll(SimpleExtendedModel.class, "id > ?", new String[]{"1"}));
        assertEquals(1L, count(SimpleExtendedModel.class));
    }

    @Test
    public void deleteInTransactionFewTest() {
        SimpleExtendedModel first = new SimpleExtendedModel();
        SimpleExtendedModel second = new SimpleExtendedModel();
        SimpleExtendedModel third = new SimpleExtendedModel();
        save(first);
        save(second);
        // Not saving last model
        assertEquals(2L, count(SimpleExtendedModel.class));
        assertEquals(2, deleteInTx(first, second, third));
        assertEquals(0L, count(SimpleExtendedModel.class));
    }

    @Test
    public void deleteInTransactionManyTest() {
        long elementNumber = 100;
        List<SimpleExtendedModel> models = new ArrayList<>();
        for (int i = 1; i <= elementNumber; i++) {
            SimpleExtendedModel model = new SimpleExtendedModel();
            models.add(model);
            // Not saving last model
            if (i < elementNumber) {
                save(model);
            }
        }
        assertEquals(elementNumber - 1, count(SimpleExtendedModel.class));
        assertEquals(elementNumber - 1, deleteInTx(models));
        assertEquals(0L, count(SimpleExtendedModel.class));
    }

    @Test
    public void saveInTransactionTest() {
        saveInTx(new SimpleExtendedModel(), new SimpleExtendedModel());
        assertEquals(2L, count(SimpleExtendedModel.class));
    }

    @Test
    public void listAllTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleExtendedModel());
        }
        List<SimpleExtendedModel> models = listAll(SimpleExtendedModel.class);
        assertEquals(100, models.size());
        for (long i = 1; i <= 100; i++) {
            assertEquals(Long.valueOf(i), models.get((int) i - 1).getId());
        }
    }

    @Test
    public void findTest() {
        save(new SimpleExtendedModel());
        save(new SimpleExtendedModel());
        List<SimpleExtendedModel> models = find(SimpleExtendedModel.class, "id = ?", "2");
        assertEquals(1, models.size());
        assertEquals(Long.valueOf(2L), models.get(0).getId());
    }

    @Test
    public void findWithQueryTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleExtendedModel());
        }
        List<SimpleExtendedModel> models = findWithQuery(SimpleExtendedModel.class, "Select * from " +
                                          NamingHelper.toTableName(SimpleExtendedModel.class) +
                                          " where id >= ? ", "50");
        for (SimpleExtendedModel model : models) {
            assertEquals(75, model.getId(), 25L);
        }
    }

    @Test
    @SuppressWarnings("all")
    public void findByIdTest() {
        save(new SimpleExtendedModel());
        assertEquals(Long.valueOf(1L), findById(SimpleExtendedModel.class, 1L).getId());
    }

    @Test
    public void findByIdIntegerTest() {
        save(new SimpleExtendedModel());
        assertEquals(Long.valueOf(1L), findById(SimpleExtendedModel.class, 1).getId());
    }

    @Test
    public void findByIdStringsNullTest() {
        save(new SimpleExtendedModel());
        assertEquals(0, findById(SimpleExtendedModel.class, new String[]{""}).size());
    }

    @Test
    public void findByIdStringsOneTest() {
        save(new SimpleExtendedModel());
        List<SimpleExtendedModel> models = findById(SimpleExtendedModel.class, "1");
        assertEquals(1, models.size());
        assertEquals(Long.valueOf(1L), models.get(0).getId());
    }

    @Test
    public void findByIdStringsTwoTest() {
        save(new SimpleExtendedModel());
        save(new SimpleExtendedModel());
        save(new SimpleExtendedModel());

        List<SimpleExtendedModel> models = findById(SimpleExtendedModel.class, "1", "3");

        assertEquals(2, models.size());
        assertEquals(Long.valueOf(1L), models.get(0).getId());
        assertEquals(Long.valueOf(3L), models.get(1).getId());
    }

    @Test
    public void findByIdStringsManyTest() {
        for (int i = 1; i <= 10; i++) {
            save(new SimpleExtendedModel());
        }

        List<SimpleExtendedModel> models = findById(SimpleExtendedModel.class, "1", "3", "6", "10");

        assertEquals(4, models.size());
        assertEquals(Long.valueOf(1L), models.get(0).getId());
        assertEquals(Long.valueOf(3L), models.get(1).getId());
        assertEquals(Long.valueOf(6L), models.get(2).getId());
        assertEquals(Long.valueOf(10L), models.get(3).getId());
    }

    @Test
    public void findByIdStringsOrderTest() {
        for (int i = 1; i <= 10; i++) {
            save(new SimpleExtendedModel());
        }

        List<SimpleExtendedModel> models = findById(SimpleExtendedModel.class, "10", "6", "3", "1");

        assertEquals(4, models.size());
        // The order of the query doesn't matter
        assertEquals(Long.valueOf(1L), models.get(0).getId());
        assertEquals(Long.valueOf(3L), models.get(1).getId());
        assertEquals(Long.valueOf(6L), models.get(2).getId());
        assertEquals(Long.valueOf(10L), models.get(3).getId());
    }

    @Test
    public void findByIdNullTest() {
        save(new SimpleExtendedModel());
        assertNull(findById(SimpleExtendedModel.class, 2L));
    }

    @Test
    public void findAllTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleExtendedModel());
        }

        Iterator<SimpleExtendedModel> cursor = findAll(SimpleExtendedModel.class);

        for (int i = 1; i <= 100; i++) {
            assertTrue(cursor.hasNext());
            SimpleExtendedModel model = cursor.next();
            assertNotNull(model);
            assertEquals(Long.valueOf(i), model.getId());
        }
    }

    @Test
    public void findAsIteratorTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleExtendedModel());
        }

        Iterator<SimpleExtendedModel> cursor = findAsIterator(SimpleExtendedModel.class, "id >= ?", "50");

        for (int i = 50; i <= 100; i++) {
            assertTrue(cursor.hasNext());
            SimpleExtendedModel model = cursor.next();
            assertNotNull(model);
            assertEquals(Long.valueOf(i), model.getId());
        }
    }

    @Test
    public void findWithQueryAsIteratorTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleExtendedModel());
        }

        Iterator<SimpleExtendedModel> cursor = findWithQueryAsIterator(SimpleExtendedModel.class,
                                                    "Select * from " +
                                                    NamingHelper.toTableName(SimpleExtendedModel.class) +
                                                    " where id >= ? ", "50");
        for (int i = 50; i <= 100; i++) {
            assertTrue(cursor.hasNext());
            SimpleExtendedModel model = cursor.next();
            assertNotNull(model);
            assertEquals(Long.valueOf(i), model.getId());
        }
    }

    @Test(expected=NoSuchElementException.class)
    public void findAsIteratorOutOfBoundsTest() {
        save(new SimpleExtendedModel());
        Iterator<SimpleExtendedModel> cursor = findAsIterator(SimpleExtendedModel.class, id, "1");
        assertTrue(cursor.hasNext());
        SimpleExtendedModel model = cursor.next();
        assertNotNull(model);
        assertEquals(Long.valueOf(1), model.getId());
        // This should throw a NoSuchElementException
        cursor.next();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void disallowRemoveCursorTest() {
        save(new SimpleExtendedModel());
        Iterator<SimpleExtendedModel> cursor = findAsIterator(SimpleExtendedModel.class, id, "1");
        assertTrue(cursor.hasNext());
        SimpleExtendedModel model = cursor.next();
        assertNotNull(model);
        assertEquals(Long.valueOf(1), model.getId());
        // This should throw a UnsupportedOperationException
        cursor.remove();
    }

    @Test
    public void vacuumTest() {
        executeQuery("Vacuum");
    }
}