package com.ece1778.keiming.assignment4.Utils;

/**
 * Created by Keiming on 10/01/2015.
 * From https://github.com/danialgoodwin/android-simply-advanced-helper/blob/master/SimplyAdvancedHelperLibrary/src/net/simplyadvanced/utils/OrientationUtils.java
 */
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Display;

/** Static methods related to device orientation. */
public class OrientationUtils {
    private OrientationUtils() {}

    /** Returns true if device in in landscape mode, otherwise false. */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /** Returns true if device in in portrait mode, otherwise false. */
    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    /** Locks the device window in landscape mode. */
    public static void setOrientationLandscape(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /** Locks the device window in portrait mode. */
    public static void setOrientationPortrait(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /** Allows user to freely use portrait or landscape mode. */
    public static void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

}