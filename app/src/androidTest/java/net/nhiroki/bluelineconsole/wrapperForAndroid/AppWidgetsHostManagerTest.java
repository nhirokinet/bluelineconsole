package net.nhiroki.bluelineconsole.wrapperForAndroid;

import android.appwidget.AppWidgetHost;
import android.os.Build;
import android.test.AndroidTestCase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.dataStore.deviceLocal.WidgetsSetting;
import net.nhiroki.bluelineconsole.dataStore.persistent.HomeScreenSetting;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class AppWidgetsHostManagerTest extends AndroidTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void garbageCollectForAppWidgetIdsTest() {
        // Due to API availability, currently GC can run on API 26 or above
        if (Build.VERSION.SDK_INT >= 26) {
            WidgetsSetting.destroyFilesForCleanTest(this.getContext());
            HomeScreenSetting.destroyFilesForCleanTest(this.getContext());

            AppWidgetsHostManager appWidgetsHostManager = new AppWidgetsHostManager(this.getContext());

            int[] allocatedAppWidgetIds = new int[8];

            for (int i = 0; i < allocatedAppWidgetIds.length; ++i) {
                allocatedAppWidgetIds[i] = appWidgetsHostManager.allocateAppWidgetId();
            }

            WidgetsSetting widgetsSettingInstance = WidgetsSetting.getInstance(this.getContext());

            List<AppWidgetsHostManager.WidgetCommand> testCommands = new ArrayList<>();

            testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, allocatedAppWidgetIds[1]));
            testCommands.get(0).command = "test";
            testCommands.get(0).heightPx = 1234;
            testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, allocatedAppWidgetIds[4]));
            testCommands.get(1).command = "test2";
            testCommands.get(1).heightPx = 1236;

            for (AppWidgetsHostManager.WidgetCommand c: testCommands) {
                widgetsSettingInstance.addWidgetCommand(c);
            }

            List<AppWidgetsHostManager.HomeScreenWidgetInfo> testHomeScreenWidgets = new ArrayList<>();

            testHomeScreenWidgets.add(new AppWidgetsHostManager.HomeScreenWidgetInfo(0, null, allocatedAppWidgetIds[2]));
            testHomeScreenWidgets.get(0).heightPx = 1234;
            testHomeScreenWidgets.add(new AppWidgetsHostManager.HomeScreenWidgetInfo(0, null, allocatedAppWidgetIds[6]));
            testHomeScreenWidgets.get(0).heightPx = 1234;

            for (AppWidgetsHostManager.HomeScreenWidgetInfo w: testHomeScreenWidgets) {
                widgetsSettingInstance.addWidgetToHomeScreen(w);
            }


            appWidgetsHostManager.garbageCollectForAppWidgetIds();

            AppWidgetHost appWidgetHost = new AppWidgetHost(this.getContext().getApplicationContext(), AppWidgetsHostManager.MY_WIDGET_HOST_ID);
            int[] appWidgetIdsAfterGC = appWidgetHost.getAppWidgetIds();

            Arrays.sort(allocatedAppWidgetIds);
            Arrays.sort(appWidgetIdsAfterGC);

            assertEquals(4, appWidgetIdsAfterGC.length);
            assertEquals(allocatedAppWidgetIds[1], appWidgetIdsAfterGC[0]);
            assertEquals(allocatedAppWidgetIds[2], appWidgetIdsAfterGC[1]);
            assertEquals(allocatedAppWidgetIds[4], appWidgetIdsAfterGC[2]);
            assertEquals(allocatedAppWidgetIds[6], appWidgetIdsAfterGC[3]);
        }
    }
}
