package com.ece1778.keiming.assignment3.Utils;

import java.util.Calendar;

/**
 * Created by Kei-Ming on 2015-02-10.
 */
public class GeneralUtils {
    private GeneralUtils() {}

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
}
