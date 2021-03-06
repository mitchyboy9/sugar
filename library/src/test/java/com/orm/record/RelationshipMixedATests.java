package com.orm.record;

import com.orm.SugarApp;
import com.orm.model.RelationshipMixedAModel;
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
@Config(sdk = 18, application = SugarApp.class, packageName = "com.orm.model", manifest = Config.NONE)
public final class RelationshipMixedATests {

    @Test
    public void emptyDatabaseTest() {
        assertEquals(0L, count(RelationshipMixedAModel.class));
        assertEquals(0L, count(SimpleAnnotatedModel.class));
    }

    @Test
    public void oneSaveTest() {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        RelationshipMixedAModel mixedAModel = new RelationshipMixedAModel(simple);

        save(simple);
        save(mixedAModel);

        assertEquals(1L, count(simple.getClass()));
        assertEquals(1L, count(mixedAModel.getClass()));
    }

    @Test
    public void twoSameSaveTest() {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        RelationshipMixedAModel mixedAModel1 = new RelationshipMixedAModel(simple);
        RelationshipMixedAModel mixedAModel2 = new RelationshipMixedAModel(simple);


        save(simple);
        save(mixedAModel1);
        save(mixedAModel2);

        assertEquals(1L, count(simple.getClass()));
        assertEquals(2L, count(mixedAModel1.getClass()));
    }

    @Test
    public void twoDifferentSaveTest() {
        SimpleAnnotatedModel anotherSimple = new SimpleAnnotatedModel();
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        RelationshipMixedAModel mixedAModel = new RelationshipMixedAModel(simple);
        RelationshipMixedAModel anotherMixedAModel = new RelationshipMixedAModel(anotherSimple);

        save(simple);
        save(anotherSimple);
        save(mixedAModel);
        save(anotherMixedAModel);

        assertEquals(2L, count(simple.getClass()));
        assertEquals(2L, count(mixedAModel.getClass()));
    }

    @Test
    public void manySameSaveTest() {
        final SimpleAnnotatedModel simple = new SimpleAnnotatedModel();
        RelationshipMixedAModel mixedAModel = null;
        save(simple);

        for (int i = 1; i <= 100; i++) {
            mixedAModel = new RelationshipMixedAModel(simple);
            save(mixedAModel);
        }

        assertEquals(1L, count(simple.getClass()));
        assertEquals(100L, count(mixedAModel.getClass()));
    }

    @Test
    public void manyDifferentSaveTest() {
        SimpleAnnotatedModel simple = null;
        RelationshipMixedAModel mixedAModel = null;

        for (int i = 1; i <= 100; i++) {
            simple = new SimpleAnnotatedModel();
            mixedAModel = new RelationshipMixedAModel(simple);

            save(simple);
            save(mixedAModel);
        }

        assertEquals(100L, count(simple.getClass()));
        assertEquals(100L, count(mixedAModel.getClass()));
    }

    @Test
    public void listAllSameTest() {
        SimpleAnnotatedModel simple = new SimpleAnnotatedModel();

        for (int i = 1; i <= 100; i++) {
            RelationshipMixedAModel mixedAModel = new RelationshipMixedAModel(simple);

            save(simple);
            save(mixedAModel);
        }

        List<RelationshipMixedAModel> models = listAll(RelationshipMixedAModel.class);
        assertEquals(100, models.size());

        for (RelationshipMixedAModel model : models) {
            assertEquals(simple.getId(), model.getSimple().getId());
        }
    }

    @Test
    public void listAllDifferentTest() {
        for (int i = 1; i <= 100; i++) {
            SimpleAnnotatedModel simple = new SimpleAnnotatedModel();

            save(simple);
            save(new RelationshipMixedAModel(simple));
        }

        List<RelationshipMixedAModel> models = listAll(RelationshipMixedAModel.class);
        assertEquals(100, models.size());

        for (RelationshipMixedAModel model : models) {
            assertEquals(model.getId(), model.getSimple().getId());
        }
    }
}
