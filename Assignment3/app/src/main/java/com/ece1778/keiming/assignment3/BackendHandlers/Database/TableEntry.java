package com.ece1778.keiming.assignment3.BackendHandlers.Database;

/**
 * Created by Kei-Ming on 2015-02-04.
 */
public class TableEntry {

    private long id;
    private String mLocation;
    private String mPath;
    private String mNote;

    // empty constructor
    public TableEntry() {

    }

    // partial constructor for adding to database w/o ID
    public TableEntry(String location, String path, String note) {
        this.mLocation = location;
        this.mPath = path;
        this.mNote = note;
    }

    // full constructor for reading from database with ID
    public TableEntry(long id, String location, String path, String note) {
        this.id = id;
        this.mLocation = location;
        this.mPath = path;
        this.mNote = note;
    }

    public long getID() {
        return this.id;
    }
    public void setID(long id) {
        this.id = id;
    }

    // Get/Set Functions for the database
    public String getLocation() {
        return this.mLocation;
    }
    public void setLocation(String value) {
        this.mLocation = value;
    }

    public String getPath() {
        return this.mPath;
    }
    public void setPath(String value) {
        this.mPath = value;
    }

    public String getNote() {
        return this.mNote;
    }
    public void setNote(String value) {
        this.mNote = value;
    }

}
