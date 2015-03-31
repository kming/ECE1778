package com.ece1778.footprints.manager;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.ece1778.footprints.BuildConfig;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by Keiming on 15/03/2015.
 */
public class MotionDetectionLocationService extends Service {

    // Debugging Tag
    private String TAG = MotionDetectionLocationService.class.getName();

    // Singleton Class Object Definition
    private static MotionDetectionLocationService manager = null;
    /**
     * PRIVATE VARIABLES - internal variables
     */
    private static SensorManager mSensorManager;
    private static SensorEventListener mSensorEventListener;
    private static boolean mMotionDetected = false;
    private static ArrayList<Float> mValues = new ArrayList<Float>();
    private static float mTotalValue = 0;
    private static final float NUMPOINTS = 4;
    private static final float THRESHOLD = 1;


    // PRIVATE CLASS VARIABLES - Location callbacks as well.
    private static LocationChangedListener mLocationChangedListener = null;
    private GoogleApiClient mGoogleApiClient = null;
    private Location mCurrentLocation = null;
    private Location mPreviousLocation = null;
    private boolean mRequestingLocationUpdates = false; // if location api is on this is true
    private LocationRequest mLocationRequest = null;
    private Context mContext = null;
    private int mFastInterval = 5000;
    private int mInterval = 20000;
    private boolean mFastDetect = true;
    private int mFastIntervalInitial = 5000;
    private int mIntervalInitial = 10000;
    private static final int FAST_INTERVAL_DEFAULT = 5000;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int ONE_MINUTE  = 1000 * 60 * 1;

    private ArrayList<Location> mLocations = null;
    private static final int NUM_LOCATIONS = 3;
    private static final double SPEED_MIN_THRESHOLD = 0.3;
    private static final double SPEED_MAX_THRESHOLD = 50;

    /**
     * DEFAULT SERVICES FUNCTIONS - This is the functions necessary for the service to run.  It is
     * what is called continuously.
     */

    // onCreate - Only called once when a new service is started.  If a services is already running,
    // this method won't be called.
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d (TAG, "onCreated"); }
        initMotionDetection();
        initManager(this);
        return;
    }

    // onStartCommand - When a services is started, this will run indefinitely in the background
    // until stopSelf() or stopService() is called.
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        super.onStartCommand(intent, flags, startID);
        if (BuildConfig.DEBUG) { Log.d(TAG, "Service Started"); }
        // start Location Tracking
        onStart();
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
        // Stop Location Tracking
        onStop();
        // remove Listener
        resetListener();
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
                    // Stop Detection of Acceleration
                    resetListener();
                    // Start Location Detection
                    setRefreshRate(mIntervalInitial, mFastIntervalInitial);
                    onResume();
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
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


    /**
     *  GOOGLE LOCATION API FUNCTIONS
     */
    private GoogleApiClient.ConnectionCallbacks mLocationCallback =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    manager.onConnected();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    manager.onSuspended();
                }
            };

    private GoogleApiClient.OnConnectionFailedListener mLocationFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    manager.onConnectionFail();
                }
            };
    private LocationListener mLocationListener =
            new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    manager.onLocationChanged(location);
                }
            };

    // Called on Resume - Needs to re-enable system.
    public static void onResume() {
        if (manager.mRequestingLocationUpdates == false) {
            manager.startLocationUpdates();
            manager.mRequestingLocationUpdates = true;
        }
        manager.mLocations.clear();
    }

    // Called on Pause - Needs to disable system. prevent hogging location services
    public static void onPause() {
        if (manager.mRequestingLocationUpdates == true) {
            manager.stopLocationUpdates();
            manager.mRequestingLocationUpdates = false;
        }
    }

    // Connection of GoogleAPI Client
    public boolean onStart() {
        if (manager.mGoogleApiClient != null &&
                !manager.mGoogleApiClient.isConnected()) {
            manager.mGoogleApiClient.connect();
            return true;
        }
        return false;
    }

    public boolean onStop() {
        if (manager.mGoogleApiClient != null &&
                manager.mGoogleApiClient.isConnected()) {
            manager.mGoogleApiClient.disconnect();
            return true;
        }
        return false;
    }
    // Initialize the google api client
    protected synchronized void buildGoogleApiClient() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Google Play Services Status: " + Integer.toString(status));
        }
        if (status != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, (Activity) mContext, 1);
            dialog.show();
        } else {
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                        .addConnectionCallbacks(mLocationCallback)
                        .addOnConnectionFailedListener(mLocationFailedListener)
                        .addApi(LocationServices.API)
                        .build();
            }
        }
    }

    // Connection Functions.  Handles the listeners and callbacks
    protected void onConnected() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "onConnected Connection Callback");}
        startLocationUpdates();
        this.onResume();
    }

    protected void onSuspended() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "onConnectedSuspended Connection Callback"); }
        this.onPause();
    }

    protected void onConnectionFail() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "onConnectionFail Listener"); }
    }

    /**
     * Location Algorithms to reduce power and # of points
     */
    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // Calculate Speed of the new location
        float speed = 0; // time is in ms.
        if (location.hasSpeed()) {
            speed = location.getSpeed();
        } else {
            float distance = location.distanceTo(currentBestLocation);
            speed = distance/(timeDelta/1000); // time is in ms.
        }
        boolean speedIsGood = (speed >= SPEED_MIN_THRESHOLD) && (speed <= SPEED_MAX_THRESHOLD);
        if (BuildConfig.DEBUG) { Log.d(TAG, Float.toString(speed)); }

        // If it's been more than two minutes since the current location, and the rate of change is
        // in walking speed, then we take it as new point
        if (isSignificantlyNewer) {
                return true; // significantly newer, take point
            // Speed is bad, but significantly newer, so check accuracy, if its better, use it
        } else if (isSignificantlyOlder) {
            // if the point is significantly older, then it must be worse
            return false;
        } else if (!speedIsGood) {
            // speed is not good, then just skip
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate ) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * Determines whether one person is stopped based on the speed of the person
     */
    protected boolean isStopped(Location location, Location currentBestLocation) {
        // no Location --> Not Stopped
        if (currentBestLocation == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isOlder = timeDelta < 0;
        boolean isNew = timeDelta < ONE_MINUTE; // if it is less than 30 seconds

        if (isNew || isOlder) {
            return false; // If the location is too new/older location, can't tell if it stopped.
        }

        // Calculate Speed of the new location
        float speed = 0; // time is in ms.
        if (location.hasSpeed()) {
            speed = location.getSpeed();
        } else {
            float distance = location.distanceTo(currentBestLocation);
            speed = distance/(timeDelta/1000); // time is in ms.
        }

        boolean speedIsGood = (speed >= SPEED_MIN_THRESHOLD) && (speed <= SPEED_MAX_THRESHOLD);
        if (BuildConfig.DEBUG) { Log.d(TAG, "Speed: " + Float.toString(speed)); }
        if (speedIsGood) {
            return false;
        } else if (speed < SPEED_MIN_THRESHOLD){
            return true;
        } else {
            return false;
        }
    }

    /**
     * STATIC HELPER FUNCTION
     */

    public static String locToString(Location location) {
        if (location == null) {
            return "";
        }
        return "(" + location.getLatitude() +
                " , " + location.getLongitude() + ")";
    }

    // get current location
    public static Location getLocation() {
        return manager.mCurrentLocation;
    }

    // get previous location
    public static Location getmPreviousLocation() {
        return manager.mPreviousLocation;
    }

    // get string representing location
    public static String getLocationString() {
        return MotionDetectionLocationService.locToString(manager.mCurrentLocation);
    }

    // get string representing previous location
    public static String getPreviousLocationString() {
        return MotionDetectionLocationService.locToString(manager.mPreviousLocation);
    }

    /**
     * HELPER FUNCTIONS
     */
    // set the location request update times
    public boolean setRefreshRate(int interval, int fastInterval) {
        // the default fastest should be the lowest it can go.
        if ((fastInterval > FAST_INTERVAL_DEFAULT) && (interval >= fastInterval)) {
            if ((mInterval == interval) && (mFastInterval == fastInterval)) {
                return true; // same as original, don't need to change.
            } else {
                this.mFastInterval = fastInterval;
                this.mInterval = interval;
                updateRequest();
                return true;
            }
        }
        return false;
    }
    // Initialize the handler.  This should only be called if the location manager is null.
    private static MotionDetectionLocationService initManager(Context context) {
        manager = new MotionDetectionLocationService();
        manager.initInitialModels(context);
        manager.setLocationRequest(
                manager.mInterval,
                manager.mFastInterval,
                LocationRequest.PRIORITY_HIGH_ACCURACY
        );
        return manager;
    }

    // Update Request - This should update the location request with the specific times.
    private void updateRequest() {
        // start and stop the location updates.  They are responsible for checking and stopping.
        stopLocationUpdates();
        startLocationUpdates();
    }

    // Initialize the internal models.
    protected void initInitialModels(Context context) {
        if (mContext == null) {
            mContext = context;
        }
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
        }
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        if (mLocations == null) {
            mLocations = new ArrayList<Location>();
        }
        mRequestingLocationUpdates = false;
    }

    protected void setLocationRequest(int interval, int fastestInterval, int priority) {
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(priority);
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, mLocationListener);
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, mLocationListener);
        }
    }

    protected void onLocationChanged(Location location) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "onConnectionChanged Listener"); }

        // Location changed,
        // See if it is a better location than the previous location
        if (isBetterLocation(location, mCurrentLocation)) {
            if (BuildConfig.DEBUG) { Log.d (TAG, "Found Better Location"); }
            mPreviousLocation = mCurrentLocation;
            mCurrentLocation = location;
            if (this.mLocationChangedListener != null) {
                // Listener exists, call it
                this.mLocationChangedListener.onChanged(location);
            }
        }
        // See if we have "stopped"
        if (isStopped(location, mCurrentLocation)) {
            if (BuildConfig.DEBUG) { Log.d (TAG, "Stopped Moving"); }
            //this.onPause();
            //this.setListener();
        } else {
            if (BuildConfig.DEBUG) { Log.d (TAG, "Moving"); }
        }

    }

    /**
     * LocationChanged Listeners
     */
    public static void setLocationChangedListener(LocationChangedListener listener) {
        manager.mLocationChangedListener = listener;
    }

    public interface LocationChangedListener {
        void onChanged(Location location);
    }
}
