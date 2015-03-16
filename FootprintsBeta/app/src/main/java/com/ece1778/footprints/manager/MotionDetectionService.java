package com.ece1778.footprints.manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ece1778.footprints.BuildConfig;

import java.util.ArrayList;

/**
 * Created by Keiming on 15/03/2015.
 */
public class MotionDetectionService extends Service {

    // Debugging Tag
    private String TAG = MotionDetectionService.class.getName();

    /**
     * PRIVATE VARIABLES - internal variables
     */

    private static SensorManager mSensorManager;
    private static SensorEventListener mSensorEventListener;
    private static boolean mMotionDetected = false;
    private static ArrayList<Float> mValues = new ArrayList<Float>();
    private static float mTotalValue = 0;
    private static final float NUMPOINTS = 5;
    private static final float THRESHOLD = 2;

    /**
     * DEFAULT SERVICES FUNCTIONS - This is the functions necessary for the service to run.  It is
     * what is called continuously.
     */

    // onCreate - Only called once when a new service is started.  If a services is already running,
    // this method won't be called.
    @Override
    public void onCreate() {
        super.onCreate();
        initMotionDetection();
        return;
    }

    // onStartCommand - When a services is started, this will run indefinitely in the background
    // until stopSelf() or stopService() is called.
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        super.onStartCommand(intent, flags, startID);

        if (BuildConfig.DEBUG) { Log.d(TAG, "Service Started"); }
        // Register Listener
        setListener();

        // Restarts if this service was killed
        return START_STICKY;
    }

    // only needed if the activity wants to bind
    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }

    // onDestroy - This is called when the service is no longer used and being destroyed.  This
    // must clean up listeners and the like.
    @Override
    public void onDestroy() {
        return;
    }

    /**
     *  PRIVATE FUNCTIONS - Performs the actual work.
     */

    private void initMotionDetection () {
        // initialize sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // initialize sensor listener
        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Significant Motion Detection
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                float sum = axisX + axisY + axisZ;
                if (mValues.size() < NUMPOINTS) {
                    mValues.add(sum);
                } else {
                    mValues.add(sum);
                    mTotalValue = mTotalValue + sum;
                    mTotalValue = mTotalValue - mValues.remove(0);
                }
                if (mTotalValue > NUMPOINTS*THRESHOLD) {
                    // Significant motion detected.
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                                "Triggered @ mTotalValue:" + Float.toString(mTotalValue)
                        );
                    }

                    // Start the Location Service --> Location Services will need to start Motion Detection again
                    // Stop Detection of Acceleration
                    resetListener();
                    // Stop Acceleration Service
                    stopSelf();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

    }

    private void setListener () {
        // motion
        mMotionDetected = false;
        mValues.clear();
        mTotalValue = 0;
        // register the listener
        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    private void resetListener () {
        // motion
        mMotionDetected = false;
        mValues.clear();
        mTotalValue = 0;
        // register the listener
        mSensorManager.unregisterListener(
                mSensorEventListener
        );
    }
}
