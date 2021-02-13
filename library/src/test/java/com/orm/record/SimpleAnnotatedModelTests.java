package com.orm.record;

import com.orm.SugarApp;
import com.orm.helper.NamingHelper;
import com.orm.model.SimpleAnnotatedModel;

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
import static com.orm.SugarRecord.deleteAll;
import static com.orm.SugarRecord.delete;
import static com.orm.SugarRecord.deleteInTx;
import static com.orm.SugarRecord.listAll;
import static com.orm.SugarRecord.findById;
import static com.orm.SugarRecord.saveInTx;
import static com.orm.SugarRecord.find;
import static com.orm.SugarRecord.findAsIterator;
import static com.orm.SugarRecord.findWithQuery;
import static com.orm.SugarRecord.findAll;
import static com.orm.SugarRecord.findWithQueryAsIterator;
import static com.orm.SugarRecord.executeQuery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18, application = SugarApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public final class SimpleAnnotatedModelTests {

    @Test
    public void emptyDatabaseTest() {
        assertEquals(0L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void oneSaveTest() {
        save(new SimpleAnnotatedModel());
        assertEquals(1L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void twoSaveTest() {
        save(new SimpleAnnotatedModel());
        save(new SimpleAnnotatedModel());
        assertEquals(2L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void manySaveTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleAnnotatedModel());
        }
        assertEquals(100L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void defaultIdTest() {
        assertEquals(1L, save(new SimpleAnnotatedModel()));
    }

    @Test
    public void whereCountTest() {
        save(new SimpleAnnotatedModel());
        save(new SimpleAnnotatedModel());
        assertEquals(1L, count(SimpleAnnotatedModel.class, "id = ?", "1"));
    }

    @Test
    public void whereNoCountTest() {
        assertEquals(0L, count(SimpleAnnotatedModel.class, "id = ?", "1"));
        save(new SimpleAnnotatedModel());
        save(new SimpleAnnotatedModel());
        assertEquals(0L, count(SimpleAnnotatedModel.class, "id = ?", "3"));
        assertEquals(0L, count(SimpleAnnotatedModel.class, "id = ?", "a"));
    }

    @Test
    public void whereBrokenCountTest() {
        save(new SimpleAnnotatedModel());
        save(new SimpleAnnotatedModel());
        assertEquals(-1L, count(SimpleAnnotatedModel.class, "di = ?", "1"));
    }

    @Test
    public void deleteTest() {
        SimpleAnnotatedModel model = new SimpleAnnotatedModel();
        save(model);
        assertEquals(1L, count(SimpleAnnotatedModel.class));
        assertTrue(delete(model));
        assertEquals(0L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void deleteUnsavedTest() {
        SimpleAnnotatedModel model = new SimpleAnnotatedModel();
        assertEquals(0L, count(SimpleAnnotatedModel.class));
        assertFalse(delete(model));
        assertEquals(0L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void deleteWrongTest() throws Exception {
        SimpleAnnotatedModel model = new SimpleAnnotatedModel();
        save(model);
        assertEquals(1L, count(SimpleAnnotatedModel.class));

        Field idField = model.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(model, Long.MAX_VALUE);

        assertFalse(delete(model));
        assertEquals(1L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void deleteAllTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleAnnotatedModel());
        }

        assertEquals(100, deleteAll(SimpleAnnotatedModel.class));
        assertEquals(0L, count(SimpleAnnotatedModel.class));
    }

    @Test
    @SuppressWarnings("all")
    public void deleteAllWhereTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleAnnotatedModel());
        }

        assertEquals(99, deleteAll(SimpleAnnotatedModel.class, "id > ?", new String[]{"1"}));
        assertEquals(1L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void deleteInTransactionFewTest() {
        SimpleAnnotatedModel first = new SimpleAnnotatedModel();
        SimpleAnnotatedModel second = new SimpleAnnotatedModel();
        SimpleAnnotatedModel third = new SimpleAnnotatedModel();
        save(first);
        save(second);
        // Not saving last model
        assertEquals(2L, count(SimpleAnnotatedModel.class));
        assertEquals(2, deleteInTx(first, second, third));
        assertEquals(0L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void deleteInTransactionManyTest() {
        List<SimpleAnnotatedModel> models = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            SimpleAnnotatedModel model = new SimpleAnnotatedModel();
            models.add(model);
            // Not saving last model
            if (i < 100) {
                save(model);
            }
        }

        assertEquals(99, count(SimpleAnnotatedModel.class));
        assertEquals(99, deleteInTx(models));
        assertEquals(0L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void saveInTransactionTest() {
        saveInTx(new SimpleAnnotatedModel(), new SimpleAnnotatedModel());
        assertEquals(2L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void listAllTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleAnnotatedModel());
        }

        List<SimpleAnnotatedModel> models = listAll(SimpleAnnotatedModel.class);
        assertEquals(100, models.size());

        for (long i = 1; i <= 100; i++) {
            assertEquals(Long.valueOf(i), models.get((int) i - 1).getId());
        }
    }

    @Test
    public void findTest() {
        save(new SimpleAnnotatedModel());
        save(new SimpleAnnotatedModel());

        List<SimpleAnnotatedModel> models = find(SimpleAnnotatedModel.class, "id = ?", "2");

        assertEquals(1, models.size());
        assertEquals(2L, models.get(0).getId().longValue());
    }

    @Test
    public void findWithQueryTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleAnnotatedModel());
        }

        List<SimpleAnnotatedModel> models = findWithQuery(SimpleAnnotatedModel.class, "Select * from " +
                        NamingHelper.toTableName(SimpleAnnotatedModel.class) +
                        " where id >= ? ", "50");

        for (SimpleAnnotatedModel model : models) {
            assertEquals(75L, model.getId(), 25L);
        }
    }

    @Test
    @SuppressWarnings("all")
    public void findByIdTest() {
        save(new SimpleAnnotatedModel());
        assertEquals(1L, findById(SimpleAnnotatedModel.class, 1L).getId().longValue());
    }

    @Test
    public void findByIdIntegerTest() {
        save(new SimpleAnnotatedModel());
        assertEquals(1L, findById(SimpleAnnotatedModel.class, 1).getId().longValue());
    }

    @Test
    public void findByIdStringsNullTest() {
        save(new SimpleAnnotatedModel());
        assertEquals(0, findById(SimpleAnnotatedModel.class, new String[]{""}).size());
    }

    @Test
    public void findByIdStringsOneTest() {
        save(new SimpleAnnotatedModel());
        List<SimpleAnnotatedModel> models = findById(SimpleAnnotatedModel.class, "1");
        assertEquals(1, models.size());
        assertEquals(1L, models.get(0).getId().longValue());
    }

    @Test
    public void findByIdStringsTwoTest() {
        save(new SimpleAnnotatedModel());
        save(new SimpleAnnotatedModel());
        save(new SimpleAnnotatedModel());
        List<SimpleAnnotatedModel> models = findById(SimpleAnnotatedModel.class, "1", "3");
        assertEquals(2, models.size());
        assertEquals(Long.valueOf(1L), models.get(0).getId());
        assertEquals(Long.valueOf(3L), models.get(1).getId());
    }

    @Test
    public void findByIdStringsManyTest() {
        for (int i = 1; i <= 10; i++) {
            save(new SimpleAnnotatedModel());
        }
        List<SimpleAnnotatedModel> models = findById(SimpleAnnotatedModel.class, "1", "3", "6", "10");
        assertEquals(4, models.size());
        assertEquals(Long.valueOf(1L), models.get(0).getId());
        assertEquals(Long.valueOf(3L), models.get(1).getId());
        assertEquals(Long.valueOf(6L), models.get(2).getId());
        assertEquals(Long.valueOf(10L), models.get(3).getId());
    }

    @Test
    public void findByIdStringsOrderTest() {
        for (int i = 1; i <= 10; i++) {
            save(new SimpleAnnotatedModel());
        }
        List<SimpleAnnotatedModel> models = findById(SimpleAnnotatedModel.class, "10", "6", "3", "1");
        assertEquals(4, models.size());
        // The order of the query doesn't matter
        assertEquals(Long.valueOf(1L), models.get(0).getId());
        assertEquals(Long.valueOf(3L), models.get(1).getId());
        assertEquals(Long.valueOf(6L), models.get(2).getId());
        assertEquals(Long.valueOf(10L), models.get(3).getId());
    }

    @Test
    public void findByIdNullTest() {
        save(new SimpleAnnotatedModel());
        assertNull(findById(SimpleAnnotatedModel.class, 2L));
    }

    @Test
    public void findAllTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleAnnotatedModel());
        }
        Iterator<SimpleAnnotatedModel> cursor = findAll(SimpleAnnotatedModel.class);
        for (int i = 1; i <= 100; i++) {
            assertTrue(cursor.hasNext());
            SimpleAnnotatedModel model = cursor.next();
            assertNotNull(model);
            assertEquals(Long.valueOf(i), model.getId());
        }
    }

    @Test
    public void findAsIteratorTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleAnnotatedModel());
        }
        Iterator<SimpleAnnotatedModel> cursor = findAsIterator(SimpleAnnotatedModel.class,
                "id >= ?", "50");
        for (int i = 50; i <= 100; i++) {
            assertTrue(cursor.hasNext());
            SimpleAnnotatedModel model = cursor.next();
            assertNotNull(model);
            assertEquals(Long.valueOf(i), model.getId());
        }
    }

    @Test
    public void findWithQueryAsIteratorTest() {
        for (int i = 1; i <= 100; i++) {
            save(new SimpleAnnotatedModel());
        }
        Iterator<SimpleAnnotatedModel> cursor = findWithQueryAsIterator(SimpleAnnotatedModel.class,
                        "Select * from " +
                                NamingHelper.toTableName(SimpleAnnotatedModel.class) +
                                " where id >= ? ", "50");
        for (int i = 50; i <= 100; i++) {
            assertTrue(cursor.hasNext());
            SimpleAnnotatedModel model = cursor.next();
            assertNotNull(model);
            assertEquals(Long.valueOf(i), model.getId());
        }
    }

    @Test(expected=NoSuchElementException.class)
    public void findAsIteratorOutOfBoundsTest() {
        save(new SimpleAnnotatedModel());
        Iterator<SimpleAnnotatedModel> cursor = findAsIterator(SimpleAnnotatedModel.class,
                "id = ?", "1");
        assertTrue(cursor.hasNext());
        SimpleAnnotatedModel model = cursor.next();
        assertNotNull(model);
        assertEquals(Long.valueOf(1), model.getId());
        // This should throw a NoSuchElementException
        cursor.next();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void disallowRemoveCursorTest() {
        save(new SimpleAnnotatedModel());
        Iterator<SimpleAnnotatedModel> cursor = findAsIterator(SimpleAnnotatedModel.class, "id = ?", "1");
        assertTrue(cursor.hasNext());
        SimpleAnnotatedModel model = cursor.next();
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