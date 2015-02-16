package com.ece1778.keiming.assignment2.classes;

/**
 * Created by Kei-Ming on 2015-01-25.
 */
public class DataEntry {
    private long id;
    private String name;
    private String food;
    private int age;

    // empty constructor
    public DataEntry() {

    }

    // partial constructor for adding to database w/o ID
    public DataEntry(String name, int age, String food) {
        this.name = name;
        this.age = age;
        this.food = food;
    }

    // full constructor for reading from database with ID
    public DataEntry(long id, String name, int age, String food) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.food = food;
    }
    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    // Get/Set Functions for the database
    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int value) {
        this.age = value;
    }

    public String getFood() {
        return food;
    }
    public void setFood(String value) {
        this.food = value;
    }

    // Will be used by the ArrayAdapter in the ListView
    // TODO: Scale this to more than just the name
    @Override
    public String toString() {
        return name;
    }
}
