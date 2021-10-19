package net.nhiroki.bluelineconsole.dataStore.deviceLocal;

import android.test.AndroidTestCase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.dataStore.deviceLocal.oldVersions.WidgetsSetting_1_2_5;
import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class WidgetSettingTest extends AndroidTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void widgetSettingBasicFunctionTest() {
        WidgetsSetting.destroyFilesForCleanTest(this.getContext());

        WidgetsSetting oldVersionInstance = WidgetsSetting.getInstance(this.getContext());

        List<AppWidgetsHostManager.WidgetCommand> testCommands = new ArrayList<>();

        testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, 123));
        testCommands.get(0).command = "test";
        testCommands.get(0).heightPx = 1234;

        testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, 124));
        testCommands.get(1).command = "test2";
        testCommands.get(1).heightPx = 1236;

        testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, 125));
        testCommands.get(2).command = "test";
        testCommands.get(2).heightPx = 1238;

        testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, 123));
        testCommands.get(3).command = "test";
        testCommands.get(3).heightPx = 1238;

        for (AppWidgetsHostManager.WidgetCommand c: testCommands) {
            oldVersionInstance.addWidgetCommand(c);
        }

        List<AppWidgetsHostManager.HomeScreenWidgetInfo> testHomeScreenWidgets = new ArrayList<>();
        testHomeScreenWidgets.add(new AppWidgetsHostManager.HomeScreenWidgetInfo(0, null, 123));
        testHomeScreenWidgets.get(0).heightPx = 1234;

        for (AppWidgetsHostManager.HomeScreenWidgetInfo w: testHomeScreenWidgets) {
            oldVersionInstance.addWidgetToHomeScreen(w);
        }

        oldVersionInstance.close();

        entriesLoadTest(testCommands, testHomeScreenWidgets);
    }

    @Test
    public void widgetSettingUpgradeFrom_1_2_5_Test() {
        WidgetsSetting.destroyFilesForCleanTest(this.getContext());

        WidgetsSetting_1_2_5 oldVersionInstance = WidgetsSetting_1_2_5.getInstance(this.getContext());

        List<AppWidgetsHostManager.WidgetCommand> testCommands = new ArrayList<>();

        testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, 123));
        testCommands.get(0).command = "test";
        testCommands.get(0).heightPx = 1234;

        testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, 124));
        testCommands.get(1).command = "test2";
        testCommands.get(1).heightPx = 1236;

        testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, 125));
        testCommands.get(2).command = "test";
        testCommands.get(2).heightPx = 1238;

        testCommands.add(new AppWidgetsHostManager.WidgetCommand(0, null, 123));
        testCommands.get(3).command = "test";
        testCommands.get(3).heightPx = 1238;

        for (AppWidgetsHostManager.WidgetCommand c: testCommands) {
            oldVersionInstance.addWidgetCommand(c);
        }

        List<AppWidgetsHostManager.HomeScreenWidgetInfo> testHomeScreenWidgets = new ArrayList<>();
        testHomeScreenWidgets.add(new AppWidgetsHostManager.HomeScreenWidgetInfo(0, null, 123));
        testHomeScreenWidgets.get(0).heightPx = 1234;

        for (AppWidgetsHostManager.HomeScreenWidgetInfo w: testHomeScreenWidgets) {
            oldVersionInstance.addWidgetToHomeScreen(w);
        }

        oldVersionInstance.close();

        entriesLoadTest(testCommands, testHomeScreenWidgets);
    }

    private void entriesLoadTest(List <AppWidgetsHostManager.WidgetCommand> expectedWidgetCommandList, List<AppWidgetsHostManager.HomeScreenWidgetInfo> expectedHomeScreenWidgetInfoList) {
        WidgetsSetting widgetsSetting = WidgetsSetting.getInstance(this.getContext());

        List<AppWidgetsHostManager.WidgetCommand> widgetCommandListFromDB = widgetsSetting.getAllWidgetCommands(new AppWidgetsHostManager(this.getContext()));

        assertEquals(expectedWidgetCommandList.size(), widgetCommandListFromDB.size());

        for (int i = 0; i < widgetCommandListFromDB.size(); ++i) {
            assertEquals(expectedWidgetCommandList.get(i).appWidgetId, widgetCommandListFromDB.get(i).appWidgetId);
            assertEquals(expectedWidgetCommandList.get(i).command, widgetCommandListFromDB.get(i).command);
            assertEquals(expectedWidgetCommandList.get(i).heightPx, widgetCommandListFromDB.get(i).heightPx);
        }

        List<AppWidgetsHostManager.HomeScreenWidgetInfo> homeScreenWidgetInfoListFromDB = widgetsSetting.getAllHomeScreenWidgets(new AppWidgetsHostManager(this.getContext()));
        assertEquals(expectedHomeScreenWidgetInfoList.size(), homeScreenWidgetInfoListFromDB.size());

        for (int i = 0; i < homeScreenWidgetInfoListFromDB.size(); ++i) {
            assertEquals(expectedHomeScreenWidgetInfoList.get(i).appWidgetId, homeScreenWidgetInfoListFromDB.get(i).appWidgetId);
            assertEquals(expectedHomeScreenWidgetInfoList.get(i).heightPx, homeScreenWidgetInfoListFromDB.get(i).heightPx);
        }

        widgetsSetting.close();
    }
}
