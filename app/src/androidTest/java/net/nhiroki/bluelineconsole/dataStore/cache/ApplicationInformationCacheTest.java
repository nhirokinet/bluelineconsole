package net.nhiroki.bluelineconsole.dataStore.cache;

import android.test.AndroidTestCase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.dataStore.cache.oldVersions.ApplicationInformationCache_1_2_5;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ApplicationInformationCacheTest extends AndroidTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());

        ApplicationInformationCache.destroyFilesForCleanTest(this.getContext());
    }

    @Test
    public void applicationInformationCacheBasicFunctionTest() {
        ApplicationInformationCache.destroyFilesForCleanTest(this.getContext());

        ApplicationInformationCache oldVersionInstance = new ApplicationInformationCache(this.getContext());

        List<ApplicationInformation> apps = new ArrayList<>();
        apps.add(new ApplicationInformation("net.nhiroki.bluelineconsole-test-1", "ja_JP", 2, "Test App", true));
        apps.add(new ApplicationInformation("net.nhiroki.bluelineconsole-test-2", "en_GB", 3, "Test App 2", false));

        for (ApplicationInformation app: apps) {
            oldVersionInstance.updateCache(app);
        }

        oldVersionInstance.close();

        entriesLoadTest(apps);
    }

    @Test
    public void applicationInformationCacheUpgradeFrom_1_2_5_Test() {
        ApplicationInformationCache.destroyFilesForCleanTest(this.getContext());

        ApplicationInformationCache_1_2_5 oldVersionInstance = new ApplicationInformationCache_1_2_5(this.getContext());

        List<ApplicationInformation> apps = new ArrayList<>();
        apps.add(new ApplicationInformation("net.nhiroki.bluelineconsole-test-1", "ja_JP", 2, "Test App", true));
        apps.add(new ApplicationInformation("net.nhiroki.bluelineconsole-test-2", "en_GB", 3, "Test App 2", false));

        for (ApplicationInformation app: apps) {
            oldVersionInstance.updateCache(app);
        }

        oldVersionInstance.close();

        entriesLoadTest(apps);
    }

    private void entriesLoadTest(List<ApplicationInformation> expectedApplicationInformationList) {
        ApplicationInformationCache applicationInformationCache = new ApplicationInformationCache(this.getContext());

        List <ApplicationInformation> appsLoaded = applicationInformationCache.getAllApplicationCaches();

        assertEquals(expectedApplicationInformationList.size(), appsLoaded.size());

        for (int i = 0; i < expectedApplicationInformationList.size(); ++i) {
            assertEquals(expectedApplicationInformationList.get(i).getLabel(), appsLoaded.get(i).getLabel());
            assertEquals(expectedApplicationInformationList.get(i).getLaunchable(), appsLoaded.get(i).getLaunchable());
            assertEquals(expectedApplicationInformationList.get(i).getLocale(), appsLoaded.get(i).getLocale());
            assertEquals(expectedApplicationInformationList.get(i).getVersion(), appsLoaded.get(i).getVersion());
            assertEquals(expectedApplicationInformationList.get(i).getPackageName(), appsLoaded.get(i).getPackageName());
        }

        applicationInformationCache.close();
    }
}
