package net.nhiroki.bluelineconsole.dataStore.cache.oldVersions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.nhiroki.bluelineconsole.dataStore.cache.ApplicationInformation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApplicationInformationCache_1_2_5 extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "application_information_cache.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static final String[] columnsInDB = {"packagename", "locale", "version", "launchable", "label"};

    public ApplicationInformationCache_1_2_5(Context context) {
        super(context, new File(context.getCacheDir(), DATABASE_NAME).toString(), null,  DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE appinfo ("+
                "  packagename TEXT NOT NULL PRIMARY KEY," +
                "  locale TEXT NOT NULL," +
                "  version INT NOT NULL," +
                "  launchable INT NOT NULL," +
                "  label TEXT NOT NULL" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is just a cache, so don't hesitate to purge
        db.execSQL("DROP TABLE IF EXISTS appinfo");
        onCreate(db);
    }

    @SuppressWarnings("unused")
    public List<ApplicationInformation> getAllApplicationCaches() {
        List<ApplicationInformation> ret = new ArrayList<>();

        Cursor curApp = this.getReadableDatabase().query("appinfo", columnsInDB, null, null, null, null, null);

        while(curApp.moveToNext()) {
            ret.add(new ApplicationInformation(
                    curApp.getString(curApp.getColumnIndex("packagename")),
                    curApp.getString(curApp.getColumnIndex("locale")),
                    curApp.getInt(curApp.getColumnIndex("version")),
                    curApp.getString(curApp.getColumnIndex("label")),
                    curApp.getInt(curApp.getColumnIndex("launchable")) != 0
            ));
        }
        curApp.close();

        return ret;
    }

    public void updateCache (ApplicationInformation application) {
        ContentValues cv = new ContentValues();
        cv.put("packagename", application.getPackageName());
        cv.put("locale", application.getLocale());
        cv.put("version", application.getVersion());
        cv.put("label", application.getLabel());
        cv.put("launchable", application.getLaunchable() ? 1 : 0);

        this.getWritableDatabase().replace("appinfo", null, cv);
    }

    @SuppressWarnings("unused")
    public void deleteCache (String packageName) {
        String[] sel = {packageName};
        this.getWritableDatabase().delete("appinfo", "packagename = ?", sel);
    }
}
