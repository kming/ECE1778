package com.ece1778.keiming.footprints.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

/**
 * Created by Kei-Ming on 2015-02-24.
 */
public class OrientationUtils {
    public static final String TAG = OrientationUtils.class.getName(); // Debug Tag

    private OrientationUtils () { /* empty constructor */ } // Used as a package of utility functions


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
