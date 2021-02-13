package com.orm.helper;

import com.orm.SugarApp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.orm.helper.ManifestHelper.DATABASE_DEFAULT_NAME;
import static com.orm.helper.ManifestHelper.getDatabaseName;
import static com.orm.helper.ManifestHelper.getDatabaseVersion;
import static com.orm.helper.ManifestHelper.getDomainPackageName;
import static com.orm.helper.ManifestHelper.isDebugEnabled;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = SugarApp.class)
public final class ManifestHelperTest {

    @Test(expected = IllegalAccessException.class)
    public void testPrivateConstructor() throws Exception {
        ManifestHelper.class.getDeclaredConstructor().newInstance();
    }

    @Test
    public void testGetDbName() {
        assertEquals(DATABASE_DEFAULT_NAME, getDatabaseName());
    }

    @Test
    public void testGetDatabaseName() {
        assertEquals(DATABASE_DEFAULT_NAME, getDatabaseName());
    }

    @Test
    public void testGetDatabaseVersion() {
        assertEquals(1, getDatabaseVersion());
    }

    @Test
    @Config(manifest = Config.NONE)
    public void getDomainPackageNameWithNoMetaDataSetInManifestReturnsEmptyString() {
        assertThat(getDomainPackageName(), is(""));
    }

    @Test
    public void testGetDebugEnabled() {
        assertFalse(isDebugEnabled());
    }
}
