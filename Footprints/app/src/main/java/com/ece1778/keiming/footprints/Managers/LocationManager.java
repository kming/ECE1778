package com.ece1778.keiming.footprints.Managers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.sql.Timestamp;


/**
 * Location Manager needs to handle several main key ideas.
 *  1 - When the location changes, it needs to call the location changed listener.
 *  2 - Instantiate Background workers to work on the location handling
 *  3 - Needs an interface that allows adjustment of updates
 */
/**
 * Created by Kei-Ming on 2015-02-24.
 */
public class LocationManager {
    // Debugging Tag
    private String TAG = LocationManager.class.getName();

    // Singleton Class Object Definition
    private static LocationManager locationManager = null;

    // private constructor for singleton class
    private LocationManager() {
    }

    // PRIVATE CLASS VARIABLES - Location callbacks as well.
    private LocationChangedListener mLocationChangedListener = null;
    private GoogleApiClient mGoogleApiClient = null;
    private Location mCurrentLocation = null;
    private Location mPreviousLocation = null;
    private boolean mRequestingLocationUpdates = false; // if location api is on this is true
    private LocationRequest mLocationRequest = null;
    private Context mContext = null;
    private int mFastInterval = 5000;
    private int mInterval = 10000;
    private static final int FAST_INTERVAL_DEFAULT = 5000;

    private GoogleApiClient.ConnectionCallbacks mLocationCallback =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    locationManager.onConnected();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    locationManager.onSuspended();
                }
            };
    private GoogleApiClient.OnConnectionFailedListener mLocationFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    locationManager.onConnectionFail();
                }
            };
    private LocationListener mLocationListener =
            new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationManager.onLocationChanged(location);
                }
            };


    /**
     * PUBLIC INTERFACE FUNCTIONS
     * onResume/onStart/onPause/onStop MUST be called, otherwise, it could result in no results
     * being returned.
     */

    // Gets Singleton Manager
    public static LocationManager getManager(Context context) {
        if (locationManager == null) {
            return initManager(context);
        } else {
            return locationManager;
        }
    }

    // Called on Resume - Needs to re-enable system.
    public static void onResume() {
        if (locationManager.mRequestingLocationUpdates == false) {
            locationManager.startLocationUpdates();
            locationManager.mRequestingLocationUpdates = true;
        }
    }
    // Called on Pause - Needs to disable system. prevent hogging location services
    public static void onPause() {
        if (locationManager.mRequestingLocationUpdates == true) {
            locationManager.stopLocationUpdates();
            locationManager.mRequestingLocationUpdates = false;
        }
    }
    // Connection of GoogleAPI Client
    public static void onStart() {
        if (!locationManager.mGoogleApiClient.isConnected()) {
            locationManager.mGoogleApiClient.connect();
        }
    }
    public static void onStop() {
        if (locationManager.mGoogleApiClient.isConnected()) {
            locationManager.mGoogleApiClient.disconnect();
        }
    }

    public static String locToString(Location location) {
        if (location == null) {
            return "";
        }
        return  "(" + location.getLatitude() +
                " , " + location.getLongitude() + ")";
    }

    /**
     *  Get/Set Functions for internal information
     */
    // get current location
    public Location getLocation() { return mCurrentLocation; }
    // get previous location
    public Location getmPreviousLocation() { return mPreviousLocation; }

    // get string representing location
    public String getLocationString () { return LocationManager.locToString(mCurrentLocation); }
    // get string representing previous location
    public String getPreviousLocationString () { return LocationManager.locToString(mPreviousLocation); }

    // set the location request update times
    public boolean setRefreshRate (int interval, int fastInterval) {
        // the default fastest should be the lowest it can go.
        if ((fastInterval > FAST_INTERVAL_DEFAULT) && (interval >= fastInterval)) {
            if ((mInterval == interval)&&(mFastInterval == fastInterval)) {
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

    /**
     * Private Helper Functions
     */
    // Initialize the handler.  This should only be called if the location manager is null.
    private static LocationManager initManager(Context context) {
        locationManager = new LocationManager();
        locationManager.initInitialModels(context);
        locationManager.setLocationRequest(
                locationManager.mInterval,
                locationManager.mFastInterval,
                LocationRequest.PRIORITY_HIGH_ACCURACY
        );
        return locationManager;
    }

    // Update Request - This should update the location request with the specific times.
    private void updateRequest(){
        // start and stop the location updates.  They are responsible for checking and stopping.
        stopLocationUpdates();
        startLocationUpdates();
    }

    // Initialize the google api client
    protected synchronized void buildGoogleApiClient() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        Log.d(TAG, "Google Play Services Status: " + Integer.toString(status));
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

    // Connection Functions.  Handles the listeners and callbacks
    protected void onConnected() {
        Log.d(TAG, "onConnected Connection Callback");
        startLocationUpdates();
        LocationManager.onResume();
    }

    protected void onSuspended() {
        Log.d(TAG, "onConnectedSuspended Connection Callback");
        LocationManager.onPause();
    }

    protected void onConnectionFail() {
        Log.d(TAG, "onConnectionFail Listener");
    }

    protected void onLocationChanged(Location location) {
        Log.d(TAG, "onConnectionChanged Listener");
        mCurrentLocation = location;
        if (this.mLocationChangedListener != null) {
            // Listener exists, call it
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            this.mLocationChangedListener.onChanged(location, timestamp);
        }
    }

    /**
     * LocationChanged Listeners
     */
    public static void setLocationChangedListener (LocationChangedListener listener) {
        locationManager.mLocationChangedListener = listener;
    }

    public interface LocationChangedListener {
        void onChanged (Location location, Timestamp timestamp);
    }
}

