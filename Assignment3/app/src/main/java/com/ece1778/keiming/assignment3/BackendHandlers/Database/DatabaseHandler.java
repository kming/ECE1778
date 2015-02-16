package com.ece1778.keiming.assignment3.BackendHandlers.Database;

/**
 * Created by Kei-Ming on 2015-02-03.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ece1778.keiming.assignment3.Utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kei-Ming on 2015-01-25.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // Makes a singleton Database
    private static DatabaseHandler databaseHandler = null;
    private static Context mContext = null;
    private String TAG = "Database Handler";

    // Database Directories
    private static String DB_PATH = null;
    private static String BACKUP_DB_DIR = null;

    // Define Database Parameters
    private static final String DATABASE_NAME = "entries.db";
    public static final String TABLE_ENTRIES = "table_entries"; // values is a keyword in sql :(
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LOCATION = "Location";
    public static final String COLUMN_PATH = "Path";
    public static final String COLUMN_NOTE = "note";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_ENTRIES
            + "("
            + COLUMN_ID   + " integer primary key autoincrement,"
            + COLUMN_LOCATION + " text not null,"
            + COLUMN_PATH  + " text not null,"
            + COLUMN_NOTE  + " text not null"
            + ");";

    /* Sets the static function helpers for the initialization of the database */
    // Private constructor to prevent other contexts from creating a new instance
    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        databaseHandler = this;
    }

    // Public function for other contexts to get access to database
    public static DatabaseHandler getHandler () {
        return databaseHandler;
    }

    // Call this to initialize the database
    public static DatabaseHandler initHandler (Context context) {
        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler(context);

            // Store the path of the backup databases so it is easy to access
            BACKUP_DB_DIR = "/data/data/" + mContext.getPackageName() + "/databases/backup/";
            File backupDir = new File (BACKUP_DB_DIR);
            if (!backupDir.exists()) {
                backupDir.mkdir();
            }

            // Store the path of the database so it is easy to access
            SQLiteDatabase db = databaseHandler.getReadableDatabase();
            DB_PATH = db.getPath();
            db.close();
        }
        return databaseHandler;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHandler.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        onCreate(db);
    }

    // This deletes a database, but also sets up an empty database after
    private void deleteDatabase () {
        mContext.deleteDatabase(DATABASE_NAME);
        databaseHandler = null;
        DatabaseHandler.initHandler(mContext);
        Log.d (TAG, "delete Database --> delete and reinitialization Sucessful");
    }

    // Use this to set the Backup storage location of the backups - static since before initialized
    public static void setBACKUP_DB_DIR (String path) {
        // Only do this before the database has been initialized
        if (databaseHandler == null) {
            BACKUP_DB_DIR = path;
        }
    }

    // Gets the path of the database
    public static String getDB_PATH () {
        return DB_PATH;
    }

    // Export Database
    public void exportDatabase(String name) throws IOException {
        // Closes all database connections to commit cache changes to memory
        databaseHandler.close();

        // Move the file over, but deletes using own method to avoid database link breaking
        String outFileName = BACKUP_DB_DIR + name + ".db";
        FileUtils.copyFile(DB_PATH, outFileName);
        Log.d (TAG, "Export Database --> Copy Sucessful");
        deleteDatabase();
    }

    // OverWrites existing Database with chosen one! --> will lose current DB info
    public void importDatabase (String name) throws IOException {
        // Closes all database connections to commit to mem
        databaseHandler.close();
        // Determines paths
        String inFileName = "";
        if (name.contains(".db")) {
            inFileName = BACKUP_DB_DIR + name;
        } else {
            inFileName = BACKUP_DB_DIR + name + ".db";
        }
        // Copy and log success
        FileUtils.copyFile(inFileName, DB_PATH);
        Log.d(TAG, "Import Database succeeded");
    }

    // Adding new Table Entry Value to database
    public void addValue(TableEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put (COLUMN_LOCATION, entry.getLocation());
        values.put(COLUMN_PATH, entry.getPath());
        values.put (COLUMN_NOTE, entry.getNote());

        // Inserting into database
        db.insert(TABLE_ENTRIES, null, values);
        db.close();
    }

    // Getting single value
    public TableEntry getValue(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[] {
                COLUMN_ID,
                COLUMN_LOCATION,
                COLUMN_PATH,
                COLUMN_NOTE
        }
                , COLUMN_ID + "=?", new String[] {
                String.valueOf(id)
        }
                , null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        TableEntry entry = new TableEntry(
                Long.parseLong(cursor.getString(0)),    // ID
                cursor.getString(1),                    // Location
                cursor.getString(2),                    // Path
                cursor.getString(3)                     // Note
        );

        db.close();
        return entry;
    }

    // Getting All Values
    public ArrayList<TableEntry> getAllValues() {
        ArrayList<TableEntry> entryList = new ArrayList<TableEntry>();

        SQLiteDatabase db = this.getWritableDatabase();
        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[] {
                COLUMN_ID,
                COLUMN_LOCATION,
                COLUMN_PATH,
                COLUMN_NOTE
        }
                , null, null, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TableEntry tableEntry = new TableEntry(
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

    // Getting Values Count
    public int getValuesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ENTRIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        db.close();

        // return count
        return cursor.getCount();
    }

    // Updating single value
    public int updateValue(TableEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put (COLUMN_LOCATION, entry.getLocation());
        values.put (COLUMN_PATH, entry.getPath());
        values.put (COLUMN_NOTE, entry.getNote());

        // updating row
        int result = db.update(TABLE_ENTRIES, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(entry.getID()) });

        db.close();
        return result;
    }

    // Deleting single value
    public void deleteValue(TableEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENTRIES, COLUMN_ID + " = ?",
                new String[] { String.valueOf(entry.getID()) });
        db.close();
    }
}
