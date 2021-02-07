package com.orm.record;

import com.orm.app.ClientApp;
import com.orm.model.RelationshipMixedBModel;
import com.orm.model.SimpleExtendedModel;

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
public class RelationshipMixedBTests {

    @Test
    public void emptyDatabaseTest() {
        assertEquals(0L, count(RelationshipMixedBModel.class));
        assertEquals(0L, count(SimpleExtendedModel.class));
    }

    @Test
    public void oneSaveTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();

        save(simple);
        save(new RelationshipMixedBModel(simple));

        assertEquals(1L, count(SimpleExtendedModel.class));
        assertEquals(1L, count(RelationshipMixedBModel.class));
    }

    @Test
    public void twoSameSaveTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();

        save(simple);
        save(new RelationshipMixedBModel(simple));
        save(new RelationshipMixedBModel(simple));

        assertEquals(1L, count(SimpleExtendedModel.class));
        assertEquals(2L, count(RelationshipMixedBModel.class));
    }

    @Test
    public void twoDifferentSaveTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        save(simple);
        SimpleExtendedModel anotherSimple = new SimpleExtendedModel();

        save(anotherSimple);
        save(new RelationshipMixedBModel(simple));
        save(new RelationshipMixedBModel(anotherSimple));

        assertEquals(2L, count(SimpleExtendedModel.class));
        assertEquals(2L, count(RelationshipMixedBModel.class));
    }

    @Test
    public void manySameSaveTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        save(simple);

        for (int i = 1; i <= 100; i++) {
            save(new RelationshipMixedBModel(simple));
        }

        assertEquals(1L, count(SimpleExtendedModel.class));
        assertEquals(100L, count(RelationshipMixedBModel.class));
    }

    @Test
    public void manyDifferentSaveTest() {
        for (int i = 1; i <= 100; i++) {
            SimpleExtendedModel simple = new SimpleExtendedModel();
            save(simple);
            save(new RelationshipMixedBModel(simple));
        }

        assertEquals(100L, count(SimpleExtendedModel.class));
        assertEquals(100L, count(RelationshipMixedBModel.class));
    }

    @Test
    public void listAllSameTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        save(simple);
        for (int i = 1; i <= 100; i++) {
            save(new RelationshipMixedBModel(simple));
        }
        List<RelationshipMixedBModel> models = listAll(RelationshipMixedBModel.class);
        assertEquals(100, models.size());
        for (RelationshipMixedBModel model : models) {
            assertEquals(simple.getId(), model.getSimple().getId());
        }
    }

    @Test
    public void listAllDifferentTest() {
        for (int i = 1; i <= 100; i++) {
            SimpleExtendedModel simple = new SimpleExtendedModel();
            save(simple);
            save(new RelationshipMixedBModel(simple));
        }
        List<RelationshipMixedBModel> models = listAll(RelationshipMixedBModel.class);
        assertEquals(100, models.size());
        for (RelationshipMixedBModel model : models) {
            assertEquals(model.getId(), model.getSimple().getId());
        }
    }
}
