package net.nhiroki.bluelineconsole.dataStore.persistent;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class URLPreferences extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "url_preferences.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static final String[] columnsInDB = {"id", "name", "display_name", "url_base", "has_query"};

    private static URLPreferences _singleton = null;


    private URLPreferences(Context context) {
        super(context, DATABASE_NAME, null,  DATABASE_VERSION);
    }

    public synchronized static URLPreferences getInstance(Context context) {
        if (_singleton == null) {
            _singleton = new URLPreferences(context);
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

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE url_info ("+
                   "  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                   "  name TEXT NOT NULL," +
                   "  display_name TEXT NOT NULL," +
                   "  url_base TEXT NOT NULL," +
                   "  has_query BOOL NOT NULL" +
                   ")"
        );
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

    public long add(URLEntry entry) {
        ContentValues cv = new ContentValues();
        cv.put("name", entry.name);
        cv.put("display_name", entry.display_name);
        cv.put("url_base", entry.url_base);
        cv.put("has_query", entry.has_query);

        return this.getWritableDatabase().insert("url_info", null, cv);
    }

    @SuppressLint("Range")
    public List<URLEntry> getAllEntries() {
        List<URLEntry> ret = new ArrayList<>();

        Cursor curEntry = this.getReadableDatabase().query("url_info", columnsInDB, null, null, null, null, "id");

        while(curEntry.moveToNext()) {
            URLEntry e = new URLEntry();
            e.id = curEntry.getInt(curEntry.getColumnIndex("id"));
            e.name = curEntry.getString(curEntry.getColumnIndex("name"));
            e.display_name = curEntry.getString(curEntry.getColumnIndex("display_name"));
            e.url_base = curEntry.getString(curEntry.getColumnIndex("url_base"));
            e.has_query = curEntry.getInt(curEntry.getColumnIndex("has_query")) != 0;

            ret.add(e);
        }

        curEntry.close();

        return ret;
    }

    public void deleteById(int id) {
        String[] args = new String[1];
        args[0] = String.valueOf(id);

        this.getWritableDatabase().delete("url_info", "id = ?", args);
    }

    public void update(URLEntry entry) {
        String[] args = new String[1];
        args[0] = String.valueOf(entry.id);

        ContentValues cv = new ContentValues();
        cv.put("name", entry.name);
        cv.put("display_name", entry.display_name);
        cv.put("url_base", entry.url_base);
        cv.put("has_query", entry.has_query);

        this.getWritableDatabase().update("url_info", cv, "id = ?", args);
    }
}
