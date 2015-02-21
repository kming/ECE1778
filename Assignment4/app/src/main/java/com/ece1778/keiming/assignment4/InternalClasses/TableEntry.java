package com.ece1778.keiming.assignment4.InternalClasses;

/**
 * Created by Kei-Ming on 2015-02-19.
 */
public class TableEntry {

    private long id;
    private String mName;
    private String mPath;
    private String mNote;

    // empty constructor
    public TableEntry() {

    }

    // partial constructor for adding to database w/o ID
    public TableEntry(String name, String path, String note) {
        this.mName = name;
        this.mPath = path;
        this.mNote = note;
    }

    // full constructor for reading from database with ID
    public TableEntry(long id, String name, String path, String note) {
        this.id = id;
        this.mName = name;
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
    public String getName() {
        return this.mName;
    }

    public void setName(String value) {
        this.mName = value;
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
