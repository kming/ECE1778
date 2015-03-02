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

/**
 * Created by Kei-Ming on 2015-02-24.
 */
public class LocationManager {
    // Singleton Class Object Definition
    private static LocationManager locationManager = null;

    // Debugging Tag
    private String TAG = "Location Manager";

    // PRIVATE CLASS VARIABLES
    private GoogleApiClient mGoogleApiClient = null;
    private Location mCurrentLocation = null;
    private boolean mRequestingLocationUpdates = false; // if location api is on this is true
    private LocationRequest mLocationRequest = null;
    private Context mContext = null;

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

    // private constructor for singleton class
    private LocationManager() {
    }

    // Public Interface Functions
    // Initialize onCreate
    public static void initHandler(Context context) {
        if (locationManager == null) {
            locationManager = new LocationManager();
            locationManager.initInitialModels(context);
            // 5sec fastest, 10sec normal, high accuracy
            locationManager.setLocationRequest(10000, 5000, LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    // get handler function
    public static LocationManager getHandler() {
        return locationManager;
    }

    // Called on Resume
    public static void onResume() {
        if (locationManager.mRequestingLocationUpdates == false) {
            locationManager.startLocationUpdates();
            locationManager.mRequestingLocationUpdates = true;
        }
    }

    // Called on Pause
    public static void onPause() {
        if (locationManager.mRequestingLocationUpdates == true) {
            locationManager.stopLocationUpdates();
            locationManager.mRequestingLocationUpdates = false;
        }
    }

    public static void onStart() {
        locationManager.mGoogleApiClient.connect();
    }
    public static void onStop() {
        locationManager.mGoogleApiClient.disconnect();
    }
    // Get Location
    public Location getLocation() {
        return mCurrentLocation;
    }

    public String getLocationString () {
        if (mCurrentLocation == null) {
            return "";
        }
        return  "(" + mCurrentLocation.getLatitude() +
                " , " + mCurrentLocation.getLongitude() + ")";
    }

    // Private Worker functions
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
    }
}

