package com.orm.util;

import android.content.ContentValues;
import android.database.Cursor;

import com.orm.SugarContext;
import com.orm.SugarRecord;
import com.orm.app.ClientApp;
import com.orm.model.TestRecord;
import com.orm.model.foreignnull.OriginRecord;
import com.orm.query.Select;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jonatan.salas
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18, application = ClientApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public final class ReflectionUtilTest {

    @Test(expected = IllegalAccessException.class)
    public void testPrivateConstructor() throws Exception {
        ReflectionUtil reflectionUtil = ReflectionUtil.class.getDeclaredConstructor().newInstance();
        Assert.assertNull(reflectionUtil);
    }

    @Test
    public void testGetTableFields() {
        List<Field> fieldList = ReflectionUtil.getTableFields(TestRecord.class);
        List<String> strings = new ArrayList<>();

        for (Field field: fieldList) {
            strings.add(field.getName());
        }

        Assert.assertTrue(strings.contains("id"));
        Assert.assertTrue(strings.contains("name"));
    }

    @Test(expected = NoSuchFieldException.class)
    public void testAddFieldValueToColumn() throws NoSuchFieldException {
        SugarContext context = SugarContext.getSugarContext();
        TestRecord record = new TestRecord();
        record.setName("lala");

        Field column = TestRecord.class.getField("name");
        ContentValues values = new ContentValues();

        ReflectionUtil.addFieldValueToColumn(values, column, record, context.getEntitiesMap());

        Assert.assertEquals(record.getName(), values.getAsString("NAME"));
    }

    @Test
    public void testSetFieldValueForId() {
        TestRecord record = new TestRecord();
        record.setName("Bla bla");

        ReflectionUtil.setFieldValueForId(record, 1L);
        Assert.assertEquals(1L, record.getId().longValue());
    }

    @Test
    public void testGetAllClasses() {
        List<Class> classes = ReflectionUtil.getDomainClasses();
        Assert.assertEquals(46, classes.size());
    }

    @Test(expected = NoSuchFieldException.class)
    public void testSetFieldValueFromCursor() throws NoSuchFieldException {
        final TestRecord record = new TestRecord().setName("bla bla");
        Long id = record.save();
        record.setId(id);

        Cursor cursor = Select.from(TestRecord.class).getCursor();

        TestRecord testRecord = new TestRecord();
        Field field = TestRecord.class.getField("name");

        ReflectionUtil.setFieldValueFromCursor(cursor, field, testRecord);
    }

    @Test
    public void testForeignNull() {
        final OriginRecord record = new OriginRecord(null,null);
        SugarRecord.save(record);
    }
}
