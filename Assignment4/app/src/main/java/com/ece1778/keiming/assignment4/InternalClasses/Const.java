package com.ece1778.keiming.assignment4.InternalClasses;

import android.os.Environment;

/**
 * Created by Kei-Ming on 2015-02-18.
 */
public class Const {

    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.
    // Using a const to allow re-configurability
    public static String MEDIA_DIRECTORY = Environment.DIRECTORY_PICTURES;

}
