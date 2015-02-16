package com.ece1778.keiming.assignment2.Database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ece1778.keiming.assignment2.classes.DataEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kei-Ming on 2015-01-25.
 */
public class DatabaseHandler extends SQLiteOpenHelper{

    // Makes a singleton Database for entire app
    private static DatabaseHandler databaseHandler = null;
    private static Context mContext = null;
    private static String CURRENT_DB_PATH = null;
    private static String BACKUP_DB_PATH_DIR = null;
    // Database table
    public static final String TABLE_ENTRIES = "table_entries"; // values is a keyword in sql :(
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_FOOD = "food";

    private static final String DATABASE_NAME = "current.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_ENTRIES
            + "("
            + COLUMN_ID   + " integer primary key autoincrement,"
            + COLUMN_NAME + " text not null,"
            + COLUMN_AGE  + " text not null,"
            + COLUMN_FOOD + " text not null"
            + ");";

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        databaseHandler = this;
    }

    public static DatabaseHandler getHandler () {
        return databaseHandler;
    }

    public static DatabaseHandler initHandler (Context context) {
        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler(context);
        }
        BACKUP_DB_PATH_DIR = "/data/data/" + mContext.getPackageName() + "/databases/backup/";

        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        CURRENT_DB_PATH = db.getPath();
        db.close();

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

    private void deleteDatabase () {
        mContext.deleteDatabase(DATABASE_NAME);
        databaseHandler = null;
        DatabaseHandler.initHandler(mContext);
    }

    public static String getDBPathDir () {
        return BACKUP_DB_PATH_DIR;
    }
    // Export Database
    public void exportDatabase(String name) throws IOException {
        // Closes all database connections to commit to mem
        databaseHandler.close();
        // Determines paths
        String outFileName = BACKUP_DB_PATH_DIR + name + ".db";

        // Checks if destination folder exists and create if not
        File createOutFile = new File(BACKUP_DB_PATH_DIR);
        if (!createOutFile.exists()){
            createOutFile.mkdir();
        }

        File fromDB = new File(CURRENT_DB_PATH);
        File toDB = new File(outFileName);
        // Copy the database to the new location
        copyFile(new FileInputStream(fromDB), new FileOutputStream(toDB));
        Log.d("export Database", "copy succeeded");
        // Delete Existing Database
        deleteDatabase();
    }

    // OverWrites existing Database with chosen one! --> will lose current DB info
    public void importDatabase (String name) throws IOException {
        // Closes all database connections to commit to mem
        databaseHandler.close();
        // Determines paths
        String inFileName = BACKUP_DB_PATH_DIR + name;

        File fromDB = new File(inFileName);
        File toDB = new File(CURRENT_DB_PATH);
        // Copy the database to the new location
        copyFile(new FileInputStream(fromDB), new FileOutputStream(toDB));
        Log.d("Import Database", "succeeded");
    }
    private static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }
    // Adding new value
    public void addValue(DataEntry DataEntry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put (COLUMN_NAME, DataEntry.getName());
        values.put (COLUMN_AGE, DataEntry.getAge());
        values.put (COLUMN_FOOD, DataEntry.getFood());

        // Inserting into database
        db.insert(TABLE_ENTRIES, null, values);
        db.close();
    }

    // Getting single value
    public DataEntry getValue(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[] {
                                                    COLUMN_ID,
                                                    COLUMN_NAME,
                                                    COLUMN_AGE,
                                                    COLUMN_FOOD
                                                }
                                , COLUMN_ID + "=?", new String[] {
                                                    String.valueOf(id)
                                                }
                                , null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        DataEntry DataEntry = new DataEntry(
                Long.parseLong(cursor.getString(0)),    // ID
                cursor.getString(1),                    // Name
                Integer.parseInt(cursor.getString(2)),  // Age
                cursor.getString(3)                     // Fav Food
        );

        db.close();
        return DataEntry;
    }

    // Getting All Values
    public List<DataEntry> getAllValues() {
        List<DataEntry> dataEntryList = new ArrayList<DataEntry>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ENTRIES;

        SQLiteDatabase db = this.getWritableDatabase();
        // Uses a cursor to query from the database.
        // Provides the strings we want from the query and the query parameters
        Cursor cursor = db.query(TABLE_ENTRIES, new String[] {
                                                    COLUMN_ID,
                                                    COLUMN_NAME,
                                                    COLUMN_AGE,
                                                    COLUMN_FOOD
                                                }
                                                , null, null, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataEntry DataEntry = new DataEntry(
                        Long.parseLong(cursor.getString(0)),    // ID
                        cursor.getString(1),                    // Name
                        Integer.parseInt(cursor.getString(2)),  // Age
                        cursor.getString(3)                     // Fav Food
                );
                // Adding contact to list
                dataEntryList.add(DataEntry);
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataEntryList;
    }

    // Getting Values Count
    public int getValuesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ENTRIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
    // Updating single value
    public int updateValue(DataEntry DataEntry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put (COLUMN_NAME, DataEntry.getName());
        values.put (COLUMN_AGE, DataEntry.getAge());
        values.put (COLUMN_FOOD, DataEntry.getFood());

        // updating row
        return db.update(TABLE_ENTRIES, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(DataEntry.getID()) });
    }

    // Deleting single value
    public void deleteValue(DataEntry DataEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENTRIES, COLUMN_ID + " = ?",
                new String[] { String.valueOf(DataEntry.getID()) });
        db.close();
    }
}
