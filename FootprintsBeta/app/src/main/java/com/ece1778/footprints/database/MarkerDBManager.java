package com.ece1778.footprints.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kei-Ming on 2015-02-24.
 */
public class MarkerDBManager extends SQLiteOpenHelper {
    public static final String TABLE_ENTRIES = "table_entries";

    // Table Columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_PICTURE = "picture";
    public static final String COLUMN_AUDIO = "audio";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_TITLE = "title";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_ENTRIES
            + "("
            + COLUMN_ID + " integer primary key autoincrement,"
            + COLUMN_TIME + " text not null,"
            + COLUMN_LOCATION + " text not null,"
            + COLUMN_PICTURE + " text,"
            + COLUMN_AUDIO + " text,"
            + COLUMN_NOTE + " text,"
            + COLUMN_TITLE + " text"
            + ");";

    // Define Database Parameters
    private static final String DATABASE_NAME = "markers.db";
    private static final int DATABASE_VERSION = 1;

    // Makes a singleton Database
    private static MarkerDBManager manager = null;
    private static Context mContext = null;

    // Database Directories
    private static String DB_PATH = null;
    private static String BACKUP_DB_DIR = null;
    private final String TAG = MarkerDBManager.class.getName();

    /* Sets the static function helpers for the initialization of the database */
    // Private constructor to prevent other contexts from creating a new instance
    private MarkerDBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        manager = this;
    }

    // Public function for other contexts to get access to database
    public static MarkerDBManager getManager(Context context) {
        if (manager == null) {
            return initManager(context);
        }
        return manager;
    }

    // Call this to initialize the database
    private static MarkerDBManager initManager(Context context) {
        manager = new MarkerDBManager(context);

        // Store the path of the backup databases so it is easy to access
        BACKUP_DB_DIR = "/data/data/" + mContext.getPackageName() + "/databases/backup/";
        File backupDir = new File(BACKUP_DB_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        // Store the path of the database so it is easy to access
        SQLiteDatabase db = manager.getReadableDatabase();
        DB_PATH = db.getPath();
        db.close();
        return manager;
    }

    // Use this to set the Backup storage location of the backups - static since before initialized
    public static void setBACKUP_DB_DIR(String path) {
        // Only do this before the database has been initialized
        if (manager == null) {
            BACKUP_DB_DIR = path;
        }
    }

    // Gets the path of the database
    public static String getDB_PATH() {
        return DB_PATH;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.w(TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        onCreate(db);
    }

    // This deletes a database, but also sets up an empty database after
    public void deleteDatabase() {
        mContext.deleteDatabase(DATABASE_NAME);
        manager = null;
        MarkerDBManager.initManager(mContext);
        if (BuildConfig.DEBUG) Log.d(TAG, "delete Database --> delete and re-init done");
    }

    // Export Database
    public void exportDatabase(String name) throws IOException {
        // Closes all database connections to commit cache changes to memory
        manager.close();

        // Move the file over, but deletes using own method to avoid database link breaking
        String outFileName = BACKUP_DB_DIR + name + ".db";
        FileUtils.copyFile(DB_PATH, outFileName);
        if (BuildConfig.DEBUG) Log.d(TAG, "Export Database --> Copy Sucessful");
        deleteDatabase();
    }

    // OverWrites existing Database with chosen one! --> will lose current DB info
    public void importDatabase(String name) throws IOException {
        // Closes all database connections to commit to mem
        manager.close();
        // Determines paths
        String inFileName = "";
        if (name.contains(".db")) {
            inFileName = BACKUP_DB_DIR + name;
        } else {
            inFileName = BACKUP_DB_DIR + name + ".db";
        }
        // Copy and log success
        FileUtils.copyFile(inFileName, DB_PATH);
        if (BuildConfig.DEBUG) Log.d(TAG, "Import Database succeeded");
    }

    // Adding new Table Entry Value to database
    public void addValue(MarkerTableEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PICTURE, entry.getPicture());
        values.put(COLUMN_AUDIO, entry.getAudio());
        values.put(COLUMN_TIME, entry.getTime());
        values.put(COLUMN_LOCATION, entry.getLocation());
        values.put(COLUMN_NOTE, entry.getNote());
        values.put(COLUMN_TITLE, entry.getTitle());

        // Inserting into database
        db.insert(TABLE_ENTRIES, null, values);
        db.close();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Add Value" + entry.getLocation());
        }
    }

    // Getting single value
    public MarkerTableEntry getValue(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[]{
                COLUMN_ID,
                COLUMN_PICTURE,
                COLUMN_AUDIO,
                COLUMN_TIME,
                COLUMN_LOCATION,
                COLUMN_NOTE,
                COLUMN_TITLE
        }
                , COLUMN_ID + "=?", new String[]{
                String.valueOf(id)
        }
                , null, null, null, null);

        MarkerTableEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = new MarkerTableEntry(
                    Long.parseLong(cursor.getString(0)),    // ID
                    cursor.getString(1),                    // Picture
                    cursor.getString(2),                    // Audio
                    cursor.getString(3),                    // time
                    cursor.getString(4),                    // location
                    cursor.getString(5),                     // Note
                    cursor.getString(6)                     // Title
            );
        }
        db.close();
        return entry;
    }

    // Only checks name to prevent duplicate
    public boolean isInDatabase(String name) {

        String checkQuery =
                "SELECT  * FROM " + TABLE_ENTRIES
                        + " where " + COLUMN_TIME
                        + " = " + name;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ENTRIES, new String[]{
                COLUMN_ID,
                COLUMN_PICTURE,
                COLUMN_AUDIO,
                COLUMN_TIME,
                COLUMN_LOCATION,
                COLUMN_NOTE,
                COLUMN_TITLE
        }
                , COLUMN_TIME + "=?", new String[]{
                name
        }
                , null, null, null, null);
        int returnCount = cursor.getCount();
        cursor.close();
        db.close();

        // return count
        if (returnCount > 0) {
            return true;
        } else {
            return false;
        }
    }

    // Getting All Values
    public ArrayList<MarkerTableEntry> getAllValues() {
        ArrayList<MarkerTableEntry> entryList = new ArrayList<MarkerTableEntry>();

        SQLiteDatabase db = this.getWritableDatabase();
        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[]{
                COLUMN_ID,
                COLUMN_PICTURE,
                COLUMN_AUDIO,
                COLUMN_TIME,
                COLUMN_LOCATION,
                COLUMN_NOTE,
                COLUMN_TITLE
        }
                , null, null, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MarkerTableEntry tableEntry = new MarkerTableEntry(
                        Long.parseLong(cursor.getString(0)),    // ID
                        cursor.getString(1),                    // Picture
                        cursor.getString(2),                    // Audio
                        cursor.getString(3),                    // time
                        cursor.getString(4),                    // location
                        cursor.getString(5),                    // Note
                        cursor.getString(6)                     // title
                );
                // Adding contact to list
                entryList.add(tableEntry);
            } while (cursor.moveToNext());
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getArray" + Integer.toString(entryList.size()));
        }
        db.close();
        // return contact list
        return entryList;
    }

    // Getting Values Count
    public int getValuesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ENTRIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int returnValue = cursor.getCount();
        cursor.close();
        db.close();

        // return count
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Get Count" + Integer.toString(returnValue));
        }
        return returnValue;
    }

    // Updating single value
    public int updateValue(MarkerTableEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PICTURE, entry.getPicture());
        values.put(COLUMN_AUDIO, entry.getAudio());
        values.put(COLUMN_TIME, entry.getTime());
        values.put(COLUMN_LOCATION, entry.getLocation());
        values.put(COLUMN_NOTE, entry.getNote());
        values.put(COLUMN_TITLE, entry.getTitle());

        // updating row
        int result = db.update(TABLE_ENTRIES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(entry.getID())});

        db.close();
        return result;
    }

    // Deleting single value
    public void deleteValue(MarkerTableEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENTRIES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(entry.getID())});
        db.close();
    }
}
