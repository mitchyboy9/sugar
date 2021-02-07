package com.orm.record;

import com.orm.app.ClientApp;
import com.orm.model.RelationshipAnnotatedModel;
import com.orm.model.SimpleAnnotatedModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static com.orm.SugarRecord.save;
import static com.orm.SugarRecord.count;
import static com.orm.SugarRecord.listAll;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18, application = ClientApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public final class RelationshipAnnotatedTests {

    @Test
    public void emptyDatabaseTest() {
        assertEquals(0L, count(RelationshipAnnotatedModel.class));
        assertEquals(0L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void oneSaveTest() {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();

        save(simple);
        save(new RelationshipAnnotatedModel(simple));

        assertEquals(1L, count(SimpleAnnotatedModel.class));
        assertEquals(1L, count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void twoSameSaveTest() {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();

        save(simple);
        save(new RelationshipAnnotatedModel(simple));
        save(new RelationshipAnnotatedModel(simple));

        assertEquals(1L, count(SimpleAnnotatedModel.class));
        assertEquals(2L, count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void twoDifferentSaveTest() {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        save(simple);
        SimpleAnnotatedModel anotherSimple = new SimpleAnnotatedModel();

        save(anotherSimple);
        save(new RelationshipAnnotatedModel(simple));
        save(new RelationshipAnnotatedModel(anotherSimple));

        assertEquals(2L, count(SimpleAnnotatedModel.class));
        assertEquals(2L, count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void manySameSaveTest() {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        save(simple);

        for (int i = 1; i <= 100; i++) {
            save(new RelationshipAnnotatedModel(simple));
        }

        assertEquals(1L, count(SimpleAnnotatedModel.class));
        assertEquals(100L, count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void manyDifferentSaveTest() {
        for (int i = 1; i <= 100; i++) {
            SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
            save(simple);
            save(new RelationshipAnnotatedModel(simple));
        }

        assertEquals(100L, count(SimpleAnnotatedModel.class));
        assertEquals(100L, count(RelationshipAnnotatedModel.class));
    }

    @Test
    public void listAllSameTest() {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        save(simple);

        for (int i = 1; i <= 100; i++) {
            save(new RelationshipAnnotatedModel(simple));
        }

        List<RelationshipAnnotatedModel> models = listAll(RelationshipAnnotatedModel.class);
        assertEquals(100, models.size());

        for (RelationshipAnnotatedModel model : models) {
            assertEquals(simple.getId(), model.getSimple().getId());
        }
    }

    @Test
    public void listAllDifferentTest() {
        for (int i = 1; i <= 100; i++) {
            SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
            save(simple);
            save(new RelationshipAnnotatedModel(simple));
        }

        List<RelationshipAnnotatedModel> models = listAll(RelationshipAnnotatedModel.class);
        assertEquals(100, models.size());

        for (RelationshipAnnotatedModel model : models) {
            assertEquals(model.getId(), model.getSimple().getId());
        }
    }
}
