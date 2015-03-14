package com.ece1778.footprints.manager;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.util.GeneralUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Kei-Ming on 2015-03-10.
 */
public class LocationServicesManager extends Service {

    // Debugging Tag
    private String TAG = LocationServicesManager.class.getName();

    // Singleton Class Object Definition
    private static LocationServicesManager locationManager = null;

    // PRIVATE CLASS VARIABLES - Location callbacks as well.
    private static LocationChangedListener mLocationChangedListener = null;
    private GoogleApiClient mGoogleApiClient = null;
    private Location mCurrentLocation = null;
    private Location mPreviousLocation = null;
    private boolean mRequestingLocationUpdates = false; // if location api is on this is true
    private LocationRequest mLocationRequest = null;
    private Context mContext = null;
    private int mFastInterval = 5000;
    private int mInterval = 10000;
    private static final int FAST_INTERVAL_DEFAULT = 5000;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public static final String LISTENER_KEY = "listener";

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

    @Override
    public void onCreate() {
        super.onCreate();
        if (locationManager == null) {
            initManager(getApplicationContext());
        }
        return;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) {Log.d(TAG, "onStartCommand");}
        GeneralUtils.performOnBackgroundThread(new Runnable () {
            @Override
            public void run() {
                if (locationManager != null) {
                    locationManager.onStart();
                }
            }
        });
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        this.onStop();
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
    public boolean onStart() {
        if (locationManager.mGoogleApiClient != null &&
                !locationManager.mGoogleApiClient.isConnected()) {
            locationManager.mGoogleApiClient.connect();
            return true;
        }
        return false;
    }

    public boolean onStop() {
        if (locationManager.mGoogleApiClient != null &&
                locationManager.mGoogleApiClient.isConnected()) {
            locationManager.mGoogleApiClient.disconnect();
            return true;
        }
        return false;
    }

    public static String locToString(Location location) {
        if (location == null) {
            return "";
        }
        return "(" + location.getLatitude() +
                " , " + location.getLongitude() + ")";
    }

    /**
     * Get/Set Functions for internal information
     */
    // get current location
    public static Location getLocation() {
        return locationManager.mCurrentLocation;
    }

    // get previous location
    public static Location getmPreviousLocation() {
        return locationManager.mPreviousLocation;
    }

    // get string representing location
    public String getLocationString() {
        return LocationManager.locToString(mCurrentLocation);
    }

    // get string representing previous location
    public String getPreviousLocationString() {
        return LocationManager.locToString(mPreviousLocation);
    }

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

    /**
     * Private Helper Functions
     */
    // Initialize the handler.  This should only be called if the location manager is null.
    private static LocationServicesManager initManager(Context context) {
        locationManager = new LocationServicesManager();
        locationManager.initInitialModels(context);
        locationManager.setLocationRequest(
                locationManager.mInterval,
                locationManager.mFastInterval,
                LocationRequest.PRIORITY_HIGH_ACCURACY
        );
        return locationManager;
    }

    // Update Request - This should update the location request with the specific times.
    private void updateRequest() {
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
        LocationServicesManager.onResume();
    }

    protected void onSuspended() {
        Log.d(TAG, "onConnectedSuspended Connection Callback");
        LocationServicesManager.onPause();
    }

    protected void onConnectionFail() {
        Log.d(TAG, "onConnectionFail Listener");
    }

    protected void onLocationChanged(Location location) {
        Log.d(TAG, "onConnectionChanged Listener");
        if (isBetterLocation(location, mCurrentLocation)) {
            mPreviousLocation = mCurrentLocation;
            mCurrentLocation = location;
            if (this.mLocationChangedListener != null) {
                // Listener exists, call it
                this.mLocationChangedListener.onChanged(location);
            }
        }

    }

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

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
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
        if (isMoreAccurate) {
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
     * LocationChanged Listeners
     */
    public static void setLocationChangedListener(LocationChangedListener listener) {
        locationManager.mLocationChangedListener = listener;
    }

    public interface LocationChangedListener {
        void onChanged(Location location);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
