package com.ece1778.keiming.footprints.Classes;

/**
 * Created by Kei-Ming on 2015-02-27.
 */
public class LocTableEntry {

    private long id;
    private String mTimeStamp;
    private String mLocation;
    private String mNote;

    // empty constructor
    public LocTableEntry() {

    }

    // partial constructor for adding to database w/o ID
    public LocTableEntry(String timestamp, String location, String note) {
        this.mTimeStamp = timestamp;
        this.mLocation = location;
        this.mNote = note;
    }

    // full constructor for reading from database with ID
    public LocTableEntry(long id, String timestamp, String location, String note) {
        this.id = id;
        this.mTimeStamp = timestamp;
        this.mLocation = location;
        this.mNote = note;
    }

    public long getID() {
        return this.id;
    }

    public void setID(long id) {
        this.id = id;
    }

    // Get/Set Functions for the database
    public String getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(String value) {
        this.mTimeStamp = value;
    }

    public String getLocation() {
        return this.mLocation;
    }

    public void setLocation(String value) {
        this.mLocation = value;
    }

    public String getNote() {
        return this.mNote;
    }

    public void setNote(String value) {
        this.mNote = value;
    }

}
