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
public class LocationDBManager extends SQLiteOpenHelper {
    public static final String TABLE_ENTRIES = "table_entries";

    // Table Columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_DIRTY = "dirty";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_ENTRIES
            + "("
            + COLUMN_ID + " integer primary key autoincrement,"
            + COLUMN_TIME + " text not null,"
            + COLUMN_LOCATION + " text not null,"
            + COLUMN_NOTE + " text not null,"
            + COLUMN_DIRTY + " text not null"
            + ");";

    // Define Database Parameters
    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;

    // Makes a singleton Database
    private static LocationDBManager manager = null;
    private static Context mContext = null;

    // Database Directories
    private static String DB_PATH = null;
    private static String BACKUP_DB_DIR = null;
    private final String TAG = LocationDBManager.class.getName();

    /* Sets the static function helpers for the initialization of the database */
    // Private constructor to prevent other contexts from creating a new instance
    private LocationDBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        manager = this;
    }

    // Public function for other contexts to get access to database
    public static LocationDBManager getManager(Context context) {
        if (manager == null) {
            return initManager(context);
        }
        return manager;
    }
    public static LocationDBManager getManager() {
        return manager;
    }

    // Call this to initialize the database
    private static LocationDBManager initManager(Context context) {
        manager = new LocationDBManager(context);

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
        LocationDBManager.initManager(mContext);
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
    public void addValue(LocTableEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, entry.getTimeStamp());
        values.put(COLUMN_LOCATION, entry.getLocation());
        values.put(COLUMN_NOTE, entry.getNote());
        values.put(COLUMN_DIRTY, "true");

        // Inserting into database
        db.insert(TABLE_ENTRIES, null, values);
        db.close();
    }

    public LocTableEntry getValue(int id) {
        return this.getValue(id, false);
    }

    // Getting single value
    public LocTableEntry getValue(int id, boolean update) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[]{
                COLUMN_ID,
                COLUMN_TIME,
                COLUMN_LOCATION,
                COLUMN_NOTE,
                COLUMN_DIRTY
        }
                , COLUMN_ID + "=?", new String[]{
                String.valueOf(id)
        }
                , null, null, null, null);

        LocTableEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = new LocTableEntry(
                    Long.parseLong(cursor.getString(0)),    // ID
                    cursor.getString(1),                    // Location
                    cursor.getString(2),                    // Path
                    cursor.getString(3)                     // Note
            );
        }
        db.close();
        // if the value is recently added, and you want to update update
        if ((cursor.getString(4).contains("true")) && (update)) {
            updateValue(entry, "false");
        }
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
                COLUMN_TIME,
                COLUMN_LOCATION,
                COLUMN_NOTE
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
    public ArrayList<LocTableEntry> getAllValues() {
        ArrayList<LocTableEntry> entryList = new ArrayList<LocTableEntry>();

        SQLiteDatabase db = this.getWritableDatabase();
        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[]{
                        COLUMN_ID,
                        COLUMN_TIME,
                        COLUMN_LOCATION,
                        COLUMN_NOTE
                },
                COLUMN_DIRTY + "=?", new String[]{
                        "true"
                }, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LocTableEntry tableEntry = new LocTableEntry(
                        Long.parseLong(cursor.getString(0)),    // ID
                        cursor.getString(1),                    // Location
                        cursor.getString(2),                    // Path
                        cursor.getString(3)                     // Note
                );
                // Adding contact to list
                entryList.add(tableEntry);
            } while (cursor.moveToNext());
        }

        db.close();
        // return contact list
        return entryList;
    }

    // Getting All Dirty Values
    public ArrayList<LocTableEntry> getAllDirtyValues() {
        ArrayList<LocTableEntry> entryList = new ArrayList<LocTableEntry>();

        SQLiteDatabase db = this.getWritableDatabase();
        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[]{
                COLUMN_ID,
                COLUMN_TIME,
                COLUMN_LOCATION,
                COLUMN_NOTE,
                COLUMN_DIRTY
        }
                , null, null, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LocTableEntry tableEntry = new LocTableEntry(
                        Long.parseLong(cursor.getString(0)),    // ID
                        cursor.getString(1),                    // Location
                        cursor.getString(2),                    // Path
                        cursor.getString(3)                     // Note
                );
                updateValue(tableEntry, "false"); // Clean all dirty values
                // Adding contact to list
                entryList.add(tableEntry);
            } while (cursor.moveToNext());
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
        return returnValue;
    }

    // Updating single value
    public int updateValue(LocTableEntry entry, String dirty) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, entry.getTimeStamp());
        values.put(COLUMN_LOCATION, entry.getLocation());
        values.put(COLUMN_NOTE, entry.getNote());
        values.put(COLUMN_DIRTY, dirty);

        // updating row
        int result = db.update(TABLE_ENTRIES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(entry.getID())});

        db.close();
        return result;
    }

    // Deleting single value
    public void deleteValue(LocTableEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENTRIES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(entry.getID())});
        db.close();
    }
}