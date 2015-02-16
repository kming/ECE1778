package com.ece1778.keiming.assignment3.BackendHandlers;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kei-Ming on 2015-02-09.
 */
public class MotionDetectionHandler {
    private MotionDetectionHandler() {
    }

    // Singleton Class
    private static MotionDetectionHandler motionHandler = null;
    private static final int SHAKE_THRESHOLD = 1200;
    // Private Class Variables
    private Context mContext = null;
    private SensorManager mManager = null;
    private Sensor mAccelerometer = null;
    private ShakeListener mShakeListener;
    private long mLastUpdate = System.currentTimeMillis();
    private ArrayList<Float> data = new ArrayList<Float>(Arrays.asList(0.0f, 0.0f, 0.0f));
    private SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            motionHandler.onSensorChanged(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public static MotionDetectionHandler getHandler() {
        return motionHandler;
    }

    public static void initHandler(Context context) {
        motionHandler = new MotionDetectionHandler();
        motionHandler.mContext = context;
        motionHandler.mManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        motionHandler.mAccelerometer = motionHandler.mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public static void onResume () {
        motionHandler.mLastUpdate = System.currentTimeMillis();
        motionHandler.data.set(0, 0.0f);
        motionHandler.data.set(1, 0.0f);
        motionHandler.data.set(2, 0.0f);

        motionHandler.mManager.registerListener(
                motionHandler.mListener,
                motionHandler.mAccelerometer,
                SensorManager.SENSOR_DELAY_GAME
        );
    }

    public static void onPause () {
        motionHandler.mManager.unregisterListener(
                motionHandler.mListener,
                motionHandler.mAccelerometer
        );

    }

    public static void flush () {
        motionHandler.mManager.flush(
                motionHandler.mListener
        );
    }
    // Sensor Changed Listener
    private void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - mLastUpdate) > 100) {
                long diffTime = (curTime - mLastUpdate);

                // when the data gets reset to zero, update the sensor with first sensor value and start from there.
                if (!((data.get(0) == 0.0f)&&(data.get(1) == 0.0f)&&(data.get(2) == 0.0f))) {
                    float x = event.values[0];
                    float last_x = data.get(0);
                    float y = event.values[1];
                    float last_y = data.get(1);
                    float z = event.values[2];
                    float last_z = data.get(2);

                    float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                    if (speed > SHAKE_THRESHOLD) {
                        Log.d("sensor", "shake detected w/ speed: " + speed);
                        Toast.makeText(mContext, "Capturing in in 1 sec", Toast.LENGTH_SHORT).show();
                        mShakeListener.onShake();
                    }
                }

                // update time and sensor values
                mLastUpdate = curTime;
                data.set(0, event.values[0]);
                data.set(1, event.values[1]);
                data.set(2, event.values[2]);
            }
        }
    }

    public static void setShakeListener (ShakeListener listener) {
        motionHandler.mShakeListener = listener;
    }

    public interface ShakeListener {
        void onShake ();
    }
}
