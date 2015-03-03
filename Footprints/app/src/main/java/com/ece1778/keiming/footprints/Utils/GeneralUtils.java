package com.ece1778.keiming.footprints.Utils;

import android.location.Location;

import java.util.Calendar;

/**
 * Created by Kei-Ming on 2015-02-24.
 */
public class GeneralUtils {

    public static final String TAG = GeneralUtils.class.getName(); // Debug Tag

    private GeneralUtils () { /* empty constructor */ } // Used as a package of utility functions

    public static String getDateString () {
        return  Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) + "/" +
                Integer.toString(Calendar.getInstance().get(Calendar.MONTH)) + "/" +
                Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
    }
    public static String getDateTimeString () {
        return  Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) + "/" +
                Integer.toString(Calendar.getInstance().get(Calendar.MONTH)) + "/" +
                Integer.toString(Calendar.getInstance().get(Calendar.YEAR)) + " " +
                Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + ":" +
                Integer.toString(Calendar.getInstance().get(Calendar.MINUTE));

    }

    public static String locationToString (Location location) {
        return  location.getLatitude() + "," +
                location.getLongitude();
    }
}
