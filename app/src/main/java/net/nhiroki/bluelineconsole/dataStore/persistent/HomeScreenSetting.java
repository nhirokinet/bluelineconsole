package net.nhiroki.bluelineconsole.dataStore.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Database for home screen setting, but this does not include widget info, due to backup treatment.
 */
public class HomeScreenSetting extends SQLiteOpenHelper {
    public static final int HOME_SCREEN_TYPE_COMMAND = 1;

    public static class HomeScreenDefaultItem {
        public int id;
        public int type;
        public String data;
    }

    private static final String DATABASE_NAME = "home_screen_setting.sqlite";
    private static final int DATABASE_VERSION = 1;

    private static HomeScreenSetting _singleton = null;

    public synchronized static HomeScreenSetting getInstance(Context context) {
        if (_singleton == null) {
            _singleton = new HomeScreenSetting(context);
        }
        return _singleton;
    }

    public static void destroyFilesForCleanTest(Context context) {
        if (_singleton != null) {
            _singleton.close();
            _singleton = null;
        }
        context.getDatabasePath(DATABASE_NAME).delete();
    }

    private HomeScreenSetting(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE home_screen_default_items (" +
                "  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "  type INTEGER NOT NULL," +
                "  data TEXT NOT NULL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do now because database structure have not been modified
    }

    @Override
    public void close() {
        _singleton = null;
        super.close();
    }

    public long addHomeScreenDefaultItem(HomeScreenDefaultItem homeScreenDefaultItem) {
        ContentValues cv = new ContentValues();
        cv.put("type", homeScreenDefaultItem.type);
        cv.put("data", homeScreenDefaultItem.data);

        return this.getWritableDatabase().insert("home_screen_default_items", null, cv);
    }

    public List<HomeScreenDefaultItem> getAllHomeScreenDefaultItems() {
        List<HomeScreenDefaultItem> ret = new ArrayList<>();

        Cursor curEntry = this.getReadableDatabase().query("home_screen_default_items", new String[]{"id", "type", "data"}, null, null, null, null, "id");

        while (curEntry.moveToNext()) {
            HomeScreenDefaultItem e = new HomeScreenDefaultItem();
            e.id = curEntry.getInt(curEntry.getColumnIndex("id"));
            e.type = curEntry.getInt(curEntry.getColumnIndex("type"));
            e.data = curEntry.getString(curEntry.getColumnIndex("data"));

            ret.add(e);
        }

        curEntry.close();

        return ret;
    }

    public HomeScreenDefaultItem getHomeScreenDefaultItemById(int id) {
        Cursor curEntry = this.getReadableDatabase().query("home_screen_default_items", new String[]{"id", "type", "data"},
                "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (curEntry.moveToNext()) {
            HomeScreenDefaultItem e = new HomeScreenDefaultItem();
            e.id = curEntry.getInt(curEntry.getColumnIndex("id"));
            e.type = curEntry.getInt(curEntry.getColumnIndex("type"));
            e.data = curEntry.getString(curEntry.getColumnIndex("data"));

            curEntry.close();
            return e;
        }
        curEntry.close();
        return null;
    }

    public int getLargestIdInHomeScreenDefaultItems() {
        Cursor curEntry = this.getReadableDatabase().query("home_screen_default_items", new String[]{"MAX(id)"}, null, null, null, null, "id");
        if (curEntry.moveToNext()) {
            return curEntry.getInt(curEntry.getColumnIndex("MAX(id)"));
        }
        return -1;
    }

    public void deleteHomeScreenDefaultItem(int id) {
        String[] args = new String[1];
        args[0] = String.valueOf(id);

        this.getWritableDatabase().delete("home_screen_default_items", "id = ?", args);
    }

    public void updateHomeScreenDefaultItem(HomeScreenDefaultItem entry) {
        String[] args = new String[1];
        args[0] = String.valueOf(entry.id);

        ContentValues cv = new ContentValues();
        cv.put("type", entry.type);
        cv.put("data", entry.data);

        this.getWritableDatabase().update("home_screen_default_items", cv, "id = ?", args);
    }
}
