package com.orm.record;

import com.orm.SugarApp;
import com.orm.model.StringFieldAnnotatedModel;
import com.orm.model.StringFieldExtendedModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.orm.SugarRecord.save;
import static com.orm.SugarRecord.findById;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18, application = SugarApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public final class StringFieldTests {
    private String string = "Test String";

    @Test
    public void nullStringExtendedTest() {
        save(new StringFieldExtendedModel());
        StringFieldExtendedModel model = findById(StringFieldExtendedModel.class, 1);
        assertNull(model.getString());
    }

    @Test
    public void nullStringAnnotatedTest() {
        save(new StringFieldAnnotatedModel());
        StringFieldAnnotatedModel model = findById(StringFieldAnnotatedModel.class, 1);
        assertNull(model.getString());
    }

    @Test
    public void stringExtendedTest() {
        save(new StringFieldExtendedModel(string));
        StringFieldExtendedModel model = findById(StringFieldExtendedModel.class, 1);
        assertEquals(string, model.getString());
    }

    @Test
    public void stringAnnotatedTest() {
        save(new StringFieldAnnotatedModel(string));
        StringFieldAnnotatedModel model = findById(StringFieldAnnotatedModel.class, 1);
        assertEquals(string, model.getString());
    }
}
