package com.ece1778.keiming.footprints.Classes;

/**
 * Created by Kei-Ming on 2015-02-27.
 */
public class MarkerTableEntry {

    private long id;
    private String mPicture;
    private String mAudio;
    private String mLocation;
    private String mNote;
    private String mTimeStamp;

    // empty constructor
    public MarkerTableEntry() {

    }

    // partial constructor for adding to database w/o ID
    // Assumes it is stored as a string for the uri
    public MarkerTableEntry(String picture, String audio, String time, String location, String note) {
        this.mPicture = picture;
        this.mAudio = audio;
        this.mLocation = location;
        this.mNote = note;
        this.mTimeStamp = time;
    }

    // full constructor for reading from database with ID
    public MarkerTableEntry(long id, String picture, String audio, String time, String location, String note) {
        this.id = id;
        this.mPicture = picture;
        this.mAudio = audio;
        this.mLocation = location;
        this.mNote = note;
        this.mTimeStamp = time;
    }

    public long getID() {
        return this.id;
    }

    public void setID(long id) {
        this.id = id;
    }

    // Get/Set Functions for the database
    public String getPicture() {
        return this.mPicture;
    }

    public void setPicture(String value) {
        this.mPicture = value;
    }

    public String getAudio() {
        return this.mAudio;
    }

    public void setAudio(String value) {
        this.mAudio = value;
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

    public String getTime() {return this.mTimeStamp; }

    public void setTime(String value) { this.mTimeStamp = value; }
}
