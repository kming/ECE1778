package com.ece1778.footprints.database;

/**
 * Created by Kei-Ming on 2015-02-27.
 */
public class NeighbourhoodTableEntry {

    public static final String SHOW = "show";
    public static final String HIDE = "hide";

    private long id;
    private String mName;
    private String mCoords;
    private String mStatus;

    // empty constructor
    public NeighbourhoodTableEntry() {

    }

    // partial constructor for adding to database w/o ID
    // Assumes it is stored as a string for the uri
    public NeighbourhoodTableEntry(String name, String coords, String status) {
        this.mName = name;
        this.mCoords = coords;
        this.mStatus = status;
    }

    // full constructor for reading from database with ID
    public NeighbourhoodTableEntry(long id, String name, String coords, String status) {
        this.id = id;
        this.mName = name;
        this.mCoords = coords;
        this.mStatus = status;
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

    public String getCoords() {
        return this.mCoords;
    }

    public void setCoords (String value) {
        this.mCoords = value;
    }

    public String getStatus() {
        return this.mStatus;
    }

    public void setStatus(String value) {
        this.mStatus = value;
    }

}
