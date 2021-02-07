package com.orm;

import com.orm.app.ClientApp;
import com.orm.model.TestRecord;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author jonatan.salas
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18, application = ClientApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public final class SugarDataSourceTest {
    private SugarDataSource<TestRecord> recordSugarDataSource;

    @Before
    public void setUp() {
        recordSugarDataSource = SugarDataSource.getInstance(TestRecord.class);
    }

    @Test
    public void testInsertAndDelete() {
        final TestRecord record = new TestRecord();
        record.setName("lalala");

        recordSugarDataSource.insert(
                record,
                record::setId,
                e -> { throw new RuntimeException(e); }
        );

        assertNotNull(record.getId());

        recordSugarDataSource.delete(
                record,
                result -> {
                    assertNotNull(result);
                    assertTrue(result);
                },
                e -> { throw new RuntimeException(e); }
        );
    }

    @Test
    public void testInsertAndFindById() {
        final TestRecord record = new TestRecord();
        record.setName("lalala");

        recordSugarDataSource.insert(
                record,
                record::setId,
                e -> { throw new RuntimeException(e); }
        );

        recordSugarDataSource.findById(
                record.getId(),
                result -> {
                    assertEquals(record.getId(), result.getId());
                    assertEquals(record.getName(), result.getName());
                },
                e -> { throw new RuntimeException(e); }
        );
    }

    @Test
    public void testInsertUpdateAndFindById() {
        final TestRecord record = new TestRecord();
        record.setName("lalala");

        recordSugarDataSource.insert(
                record,
                record::setId,
                e -> { throw new RuntimeException(e); }
        );

        record.setName("fulano");
        recordSugarDataSource.update(
                record,
                id -> assertEquals(record.getId(), id),
                e -> { throw new RuntimeException(e); }
        );

        recordSugarDataSource.findById(
                record.getId(),
                result -> {
                    assertEquals(record.getId(), result.getId());
                    assertEquals("fulano", result.getName());
                },
                e -> { throw new RuntimeException(e); }
        );
    }

    @Test
    public void testInsertAndListAll() {
        final TestRecord record = new TestRecord();
        record.setName("lalala");

        recordSugarDataSource.insert(
                record,
                record::setId,
                e -> { throw new RuntimeException(e); }
        );

        final TestRecord record1 = new TestRecord();
        record1.setName("fulano");

        recordSugarDataSource.insert(
                record1,
                record1::setId,
                e -> { throw new RuntimeException(e); }
        );

        final TestRecord record2 = new TestRecord();
        record2.setName("mengano");

        recordSugarDataSource.insert(
                record2,
                record2::setId,
                e -> { throw new RuntimeException(e); }
        );

        recordSugarDataSource.listAll(
                null,
                list -> assertEquals(3, list.size()),
                e -> { throw new RuntimeException(e); }
        );

        recordSugarDataSource.deleteAll(
                count -> assertEquals(3, count.intValue()),
                e -> { throw new RuntimeException(e); }
        );
    }

    @Test
    public void testInsertAndCount() {
        final TestRecord record = new TestRecord();
        record.setName("lalala");

        recordSugarDataSource.insert(
                record,
                record::setId,
                e -> { throw new RuntimeException(e); }
        );

        final TestRecord record1 = new TestRecord();
        record1.setName("fulano");

        recordSugarDataSource.insert(
                record1,
                record1::setId,
                e -> { throw new RuntimeException(e); }
        );


        final TestRecord record2 = new TestRecord();
        record2.setName("mengano");

        recordSugarDataSource.insert(
                record2,
                record2::setId,
                e -> { throw new RuntimeException(e); }
        );

        recordSugarDataSource.count(
                count -> assertEquals(3, count.longValue()),
                e -> { throw new RuntimeException(e); }
        );
    }

    @Test
    public void testInsertAndGetCursor() {
        final TestRecord record = new TestRecord();
        record.setName("lalala");

        recordSugarDataSource.insert(
                record,
                record::setId,
                e -> { throw new RuntimeException(e); }
        );

        final TestRecord record1 = new TestRecord();
        record1.setName("fulano");

        recordSugarDataSource.insert(
                record1,
                record1::setId,
                e -> { throw new RuntimeException(e); }
        );


        final TestRecord record2 = new TestRecord();
        record2.setName("mengano");

        recordSugarDataSource.insert(
                record2,
                record2::setId,
                e -> { throw new RuntimeException(e); }
        );

        recordSugarDataSource.listAll(
                null,
                list -> assertEquals(3, list.size()),
                e -> { throw new RuntimeException(e); }
        );

        recordSugarDataSource.query(
                null,
                null,
                null,
                null,
                null,
                Assert::assertNotNull,
                e -> { throw new RuntimeException(e); }
        );
    }

    @Test
    public void bulkInsertAndListAllTest() {
        final TestRecord record = new TestRecord();
        record.setName("lalala");

        final TestRecord record1 = new TestRecord();
        record1.setName("fulano");

        final TestRecord record2 = new TestRecord();
        record2.setName("mengano");

        final List<TestRecord> list = new ArrayList<>();
        list.add(record);
        list.add(record1);
        list.add(record2);

        recordSugarDataSource.bulkInsert(
                list,
                ids -> {
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setId(ids.get(i));
                    }
                },
                e -> { throw new RuntimeException(e); }
        );

        recordSugarDataSource.listAll(
                null,
                testRecords -> {
                    for (int i = 0; i < list.size(); i++) {
                        TestRecord record11 = list.get(i);
                        TestRecord record21 = testRecords.get(i);

                        assertEquals(record11.getId(), record21.getId());
                        assertEquals(record11.getName(), record21.getName());
                    }
                },
                e -> { throw new RuntimeException(e); }
        );
    }

    @Test
    public void nullFindById() {
        TestRecord record = new TestRecord();
        record.setId(0L);

        recordSugarDataSource.findById(
                record.getId(),
                Assert::assertNull,
                e -> { throw new RuntimeException(e); }
        );
    }

    @Test
    public void testNullListAll() {
        recordSugarDataSource.listAll(
                null,
                Assert::assertNull,
                e -> assertNotNull(e.getMessage())
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullConstructor() {
        SugarDataSource.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("all")
    public void testCheckNotNull() {
        TestRecord record = null;
        recordSugarDataSource.checkNotNull(record);
    }
}
