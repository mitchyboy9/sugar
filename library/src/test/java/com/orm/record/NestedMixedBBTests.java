package com.orm.record;

import com.orm.SugarApp;
import com.orm.model.NestedMixedBBModel;
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
@Config(sdk = 18, application = SugarApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public class NestedMixedBBTests {

    @Test
    public void emptyDatabaseTest() {
        assertEquals(0L, count(NestedMixedBBModel.class));
        assertEquals(0L, count(RelationshipMixedBModel.class));
        assertEquals(0L, count(SimpleExtendedModel.class));
    }

    @Test
    public void oneSaveTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        save(simple);
        RelationshipMixedBModel nested = new RelationshipMixedBModel(simple);

        save(nested);
        save(new NestedMixedBBModel(nested));

        assertEquals(1L, count(SimpleExtendedModel.class));
        assertEquals(1L, count(RelationshipMixedBModel.class));
        assertEquals(1L, count(NestedMixedBBModel.class));
    }

    @Test
    public void twoSameSaveTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        save(simple);
        RelationshipMixedBModel nested = new RelationshipMixedBModel(simple);

        save(nested);
        save(new NestedMixedBBModel(nested));
        save(new NestedMixedBBModel(nested));

        assertEquals(1L, count(SimpleExtendedModel.class));
        assertEquals(1L, count(RelationshipMixedBModel.class));
        assertEquals(2L, count(NestedMixedBBModel.class));
    }

    @Test
    public void twoDifferentSaveTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        save(simple);
        SimpleExtendedModel anotherSimple = new SimpleExtendedModel();
        save(anotherSimple);
        RelationshipMixedBModel nested = new RelationshipMixedBModel(simple);
        save(nested);
        RelationshipMixedBModel anotherNested = new RelationshipMixedBModel(anotherSimple);

        save(anotherNested);
        save(new NestedMixedBBModel(nested));
        save(new NestedMixedBBModel(anotherNested));

        assertEquals(2L, count(SimpleExtendedModel.class));
        assertEquals(2L, count(RelationshipMixedBModel.class));
        assertEquals(2L, count(NestedMixedBBModel.class));
    }

    @Test
    public void manySameSaveTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        save(simple);
        RelationshipMixedBModel nested = new RelationshipMixedBModel(simple);
        save(nested);

        for (int i = 1; i <= 100; i++) {
            save(new NestedMixedBBModel(nested));
        }

        assertEquals(1L, count(SimpleExtendedModel.class));
        assertEquals(1L, count(RelationshipMixedBModel.class));
        assertEquals(100L, count(NestedMixedBBModel.class));
    }

    @Test
    public void manyDifferentSaveTest() {
        for (int i = 1; i <= 100; i++) {
            SimpleExtendedModel simple = new SimpleExtendedModel();
            save(simple);
            RelationshipMixedBModel nested = new RelationshipMixedBModel(simple);
            save(nested);
            save(new NestedMixedBBModel(nested));
        }

        assertEquals(100L, count(SimpleExtendedModel.class));
        assertEquals(100L, count(RelationshipMixedBModel.class));
        assertEquals(100L, count(NestedMixedBBModel.class));
    }

    @Test
    public void listAllSameTest() {
        SimpleExtendedModel simple = new SimpleExtendedModel();
        save(simple);
        RelationshipMixedBModel nested = new RelationshipMixedBModel(simple);
        save(nested);

        for (int i = 1; i <= 100; i++) {
            save(new NestedMixedBBModel(nested));
        }

        List<NestedMixedBBModel> models = listAll(NestedMixedBBModel.class);
        assertEquals(100, models.size());

        for (NestedMixedBBModel model : models) {
            assertEquals(nested.getId(), model.getNested().getId());
            assertEquals(simple.getId(), model.getNested().getSimple().getId());
        }
    }

    @Test
    public void listAllDifferentTest() {
        for (int i = 1; i <= 100; i++) {
            SimpleExtendedModel simple = new SimpleExtendedModel();
            save(simple);
            RelationshipMixedBModel nested = new RelationshipMixedBModel(simple);
            save(nested);
            save(new NestedMixedBBModel(nested));
        }

        List<NestedMixedBBModel> models = listAll(NestedMixedBBModel.class);
        assertEquals(100, models.size());

        for (NestedMixedBBModel model : models) {
            assertEquals(model.getId(), model.getNested().getId());
            assertEquals(model.getId(), model.getNested().getSimple().getId());
        }
    }
}
