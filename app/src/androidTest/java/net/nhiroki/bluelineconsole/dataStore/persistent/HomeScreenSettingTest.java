package net.nhiroki.bluelineconsole.dataStore.persistent;

import android.test.AndroidTestCase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.nhiroki.bluelineconsole.dataStore.persistent.oldVersions.HomeScreenSetting_1_2_5;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class HomeScreenSettingTest extends AndroidTestCase {
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void homeScreenSettingBasicFunctionTest() {
        HomeScreenSetting.destroyFilesForCleanTest(this.getContext());

        HomeScreenSetting oldVersionInstance = HomeScreenSetting.getInstance(this.getContext());

        assertEquals(0, oldVersionInstance.getLargestIdInHomeScreenDefaultItems());

        List<HomeScreenSetting.HomeScreenDefaultItem> homeScreenDefaultItemList = new ArrayList<>();

        homeScreenDefaultItemList.add(new HomeScreenSetting.HomeScreenDefaultItem());
        homeScreenDefaultItemList.get(0).type = 123;
        homeScreenDefaultItemList.get(0).data = "abc";
        homeScreenDefaultItemList.add(new HomeScreenSetting.HomeScreenDefaultItem());
        homeScreenDefaultItemList.get(1).type = 124;
        homeScreenDefaultItemList.get(1).data = "abd";

        for (HomeScreenSetting.HomeScreenDefaultItem item: homeScreenDefaultItemList) {
            oldVersionInstance.addHomeScreenDefaultItem(item);
        }

        assertEquals(2, oldVersionInstance.getLargestIdInHomeScreenDefaultItems());

        oldVersionInstance.close();
        entriesLoadTest(homeScreenDefaultItemList);
    }

    @Test
    public void homeScreenSettingUpgradeFrom_1_2_5_Test() {
        HomeScreenSetting.destroyFilesForCleanTest(this.getContext());

        HomeScreenSetting_1_2_5 oldVersionInstance = HomeScreenSetting_1_2_5.getInstance(this.getContext());

        List<HomeScreenSetting.HomeScreenDefaultItem> homeScreenDefaultItemList = new ArrayList<>();

        homeScreenDefaultItemList.add(new HomeScreenSetting.HomeScreenDefaultItem());
        homeScreenDefaultItemList.get(0).type = 123;
        homeScreenDefaultItemList.get(0).data = "abc";
        homeScreenDefaultItemList.add(new HomeScreenSetting.HomeScreenDefaultItem());
        homeScreenDefaultItemList.get(1).type = 124;
        homeScreenDefaultItemList.get(1).data = "abd";

        for (HomeScreenSetting.HomeScreenDefaultItem item: homeScreenDefaultItemList) {
            oldVersionInstance.addHomeScreenDefaultItem(item);
        }

        oldVersionInstance.close();
        entriesLoadTest(homeScreenDefaultItemList);
    }

    private void entriesLoadTest(List<HomeScreenSetting.HomeScreenDefaultItem> expectedHomeScreenDefaultItems) {
        HomeScreenSetting homeScreenSetting = HomeScreenSetting.getInstance(this.getContext());

        List<HomeScreenSetting.HomeScreenDefaultItem> homeScreenDefaultItemsFromDB = homeScreenSetting.getAllHomeScreenDefaultItems();

        assertEquals(expectedHomeScreenDefaultItems.size(), homeScreenDefaultItemsFromDB.size());

        for (int i = 0; i < expectedHomeScreenDefaultItems.size(); ++i) {
            assertEquals(expectedHomeScreenDefaultItems.get(i).type, homeScreenDefaultItemsFromDB.get(i).type);
            assertEquals(expectedHomeScreenDefaultItems.get(i).data, homeScreenDefaultItemsFromDB.get(i).data);
        }

        assertEquals(expectedHomeScreenDefaultItems.size(), homeScreenSetting.getLargestIdInHomeScreenDefaultItems());

        homeScreenSetting.close();
    }
}
