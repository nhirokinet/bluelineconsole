package net.nhiroki.bluelineconsole.dataStore.deviceLocal;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.preference.PreferenceManager;

import net.nhiroki.bluelineconsole.wrapperForAndroid.AppWidgetsHostManager;

import java.util.ArrayList;
import java.util.List;

public class WidgetsSetting extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "widgets_setting.sqlite";
    private static final int DATABASE_VERSION = 1;

    private static final String PREFERENCE_FLAG_WIDGETS_SETTING_EXISTS = "flag_widgets_setting_exists";

    private static WidgetsSetting _singleton = null;

    private static boolean migrationLostchecked = false;
    private static boolean migrationLost = false;

    private final Context context;


    public static boolean migrationLostHappened(Context context) {
        if (migrationLostchecked) {
            return migrationLost;
        }

        migrationLost = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFERENCE_FLAG_WIDGETS_SETTING_EXISTS, false) &&
                !context.getDatabasePath(DATABASE_NAME).exists();
        migrationLostchecked = true;

        return migrationLost;
    }

    public static void resetMigrationLostFlag(Context context) {
        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEdit.putBoolean(PREFERENCE_FLAG_WIDGETS_SETTING_EXISTS, false);
        prefEdit.apply();
        migrationLost = false;
        migrationLostchecked = false;
    }

    private static void setDataExistsFlag(Context context) {
        SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEdit.putBoolean(PREFERENCE_FLAG_WIDGETS_SETTING_EXISTS, true);
        prefEdit.apply();
    }

    public synchronized static WidgetsSetting getInstance(Context context) {
        if (_singleton == null) {
            _singleton = new WidgetsSetting(context);
        }
        return _singleton;
    }

    private WidgetsSetting(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE home_screen_widgets (" +
                "  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "  app_widget_id INTEGER NOT NULL," +
                "  height_px INTEGER NOT NULL" +
                ")");
        db.execSQL(
                "CREATE TABLE widget_commands (" +
                "  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "  command TEXT NOT NULL," +
                "  abbreviation INTEGER NOT NULL," +
                "  app_widget_id INTEGER NOT NULL," +
                "  height_px INTEGER NOT NULL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do now because database structure have not been modified
    }

    public long addWidgetToHomeScreen(AppWidgetsHostManager.HomeScreenWidgetInfo widgetInfo) {
        setDataExistsFlag(this.context);
        ContentValues cv = new ContentValues();
        cv.put("app_widget_id", widgetInfo.appWidgetId);
        cv.put("height_px", widgetInfo.heightPx);

        return this.getWritableDatabase().insert("home_screen_widgets", null, cv);
    }

    public List<AppWidgetsHostManager.HomeScreenWidgetInfo> getAllHomeScreenWidgets(AppWidgetsHostManager appWidgetsHostManager) {
        List<AppWidgetsHostManager.HomeScreenWidgetInfo> ret = new ArrayList<>();

        Cursor curEntry = this.getReadableDatabase().query("home_screen_widgets", new String[]{"id", "app_widget_id", "height_px"}, null, null, null, null, null);

        while (curEntry.moveToNext()) {
            int appWidgetId = curEntry.getInt(curEntry.getColumnIndex("app_widget_id"));
            AppWidgetProviderInfo info = appWidgetsHostManager.getAppWidgetInfo(appWidgetId);
            AppWidgetsHostManager.HomeScreenWidgetInfo e = new AppWidgetsHostManager.HomeScreenWidgetInfo(curEntry.getInt(curEntry.getColumnIndex("id")), info, appWidgetId);
            e.heightPx = curEntry.getInt(curEntry.getColumnIndex("height_px"));

            ret.add(e);
        }

        curEntry.close();

        return ret;
    }

    public AppWidgetsHostManager.HomeScreenWidgetInfo getHomeScreenById(AppWidgetsHostManager appWidgetsHostManager, int id) {
        Cursor curEntry = this.getReadableDatabase().query("home_screen_widgets", new String[]{"id", "app_widget_id", "height_px"},
                                                  "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (curEntry.moveToNext()) {
            int appWidgetId = curEntry.getInt(curEntry.getColumnIndex("app_widget_id"));
            AppWidgetProviderInfo info = appWidgetsHostManager.getAppWidgetInfo(appWidgetId);
            AppWidgetsHostManager.HomeScreenWidgetInfo e = new AppWidgetsHostManager.HomeScreenWidgetInfo(curEntry.getInt(curEntry.getColumnIndex("id")), info, appWidgetId);
            e.heightPx = curEntry.getInt(curEntry.getColumnIndex("height_px"));

            curEntry.close();
            return e;
        }
        curEntry.close();
        return null;
    }

    public void deleteHomeScreenWidgetById(int id) {
        String[] args = new String[1];
        args[0] = String.valueOf(id);

        this.getWritableDatabase().delete("home_screen_widgets", "id = ?", args);
    }

    public void updateHomeScreenWidgetInfo(AppWidgetsHostManager.HomeScreenWidgetInfo entry) {
        String[] args = new String[1];
        args[0] = String.valueOf(entry.id);

        ContentValues cv = new ContentValues();
        cv.put("height_px", entry.heightPx);

        this.getWritableDatabase().update("home_screen_widgets", cv, "id = ?", args);
    }

    public List<AppWidgetsHostManager.WidgetCommand> getAllWidgetCommands(AppWidgetsHostManager appWidgetsHostManager) {
        List<AppWidgetsHostManager.WidgetCommand> ret = new ArrayList<>();

        Cursor curEntry = this.getReadableDatabase().query("widget_commands", new String[]{"id", "command", "abbreviation", "app_widget_id", "height_px"}, null, null, null, null, null);

        while (curEntry.moveToNext()) {
            int appWidgetId = curEntry.getInt(curEntry.getColumnIndex("app_widget_id"));
            AppWidgetProviderInfo info = appWidgetsHostManager.getAppWidgetInfo(appWidgetId);
            AppWidgetsHostManager.WidgetCommand e = new AppWidgetsHostManager.WidgetCommand(curEntry.getInt(curEntry.getColumnIndex("id")), info, appWidgetId);
            e.heightPx = curEntry.getInt(curEntry.getColumnIndex("height_px"));
            if (info != null && e.heightPx == -1) {
                e.heightPx = info.minHeight;
            }
            e.abbreviation = curEntry.getInt(curEntry.getColumnIndex("abbreviation")) > 0;
            e.command = curEntry.getString(curEntry.getColumnIndex("command"));

            ret.add(e);
        }

        curEntry.close();

        return ret;
    }

    public AppWidgetsHostManager.WidgetCommand getWidgetCommandById(AppWidgetsHostManager appWidgetsHostManager, int id) {
        Cursor curEntry = this.getReadableDatabase().query("widget_commands", new String[]{"id", "command", "abbreviation", "app_widget_id", "height_px"},
                                                  "id = ?", new String[]{String.valueOf(id)},  null, null, null);

        if (curEntry.moveToNext()) {
            int appWidgetId = curEntry.getInt(curEntry.getColumnIndex("app_widget_id"));
            AppWidgetProviderInfo info = appWidgetsHostManager.getAppWidgetInfo(appWidgetId);
            AppWidgetsHostManager.WidgetCommand e = new AppWidgetsHostManager.WidgetCommand(curEntry.getInt(curEntry.getColumnIndex("id")), info, appWidgetId);
            e.heightPx = curEntry.getInt(curEntry.getColumnIndex("height_px"));
            e.abbreviation = curEntry.getInt(curEntry.getColumnIndex("abbreviation")) > 0;
            e.command = curEntry.getString(curEntry.getColumnIndex("command"));

            curEntry.close();
            return e;
        }

        curEntry.close();
        return null;
    }

    public long addWidgetCommand(AppWidgetsHostManager.WidgetCommand entry) {
        setDataExistsFlag(this.context);

        ContentValues cv = new ContentValues();
        cv.put("command", entry.command);
        cv.put("abbreviation", entry.abbreviation ? 1 : 0);
        cv.put("app_widget_id", entry.appWidgetId);
        cv.put("height_px", entry.heightPx);

        return this.getWritableDatabase().insert("widget_commands", null, cv);

    }

    public void deleteWidgetCommandById(int id) {
        String[] args = new String[1];
        args[0] = String.valueOf(id);

        this.getWritableDatabase().delete("widget_commands", "id = ?", args);
    }

    public void updateWidgetCommandHeightPxByAppWidgetId(int appWidgetId, int heightPx) {
        String[] args = new String[1];
        args[0] = String.valueOf(appWidgetId);

        ContentValues cv = new ContentValues();
        cv.put("height_px", heightPx);

        this.getWritableDatabase().update("widget_commands", cv, "app_widget_id = ?", args);
    }

    public void updateWidgetCommand(AppWidgetsHostManager.WidgetCommand entry) {
        String[] args = new String[1];
        args[0] = String.valueOf(entry.id);

        ContentValues cv = new ContentValues();
        cv.put("command", entry.command);
        cv.put("abbreviation", entry.abbreviation ? 1 : 0);
        cv.put("height_px", entry.heightPx);

        this.getWritableDatabase().update("widget_commands", cv, "id = ?", args);
    }
}
