package com.ece1778.footprints.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.R;
import com.ece1778.footprints.database.*;
import com.ece1778.footprints.manager.LocationManager;
import com.ece1778.footprints.ui.camera.CameraActivity;
import com.ece1778.footprints.ui.marker.AddMarkerActivity;
import com.ece1778.footprints.util.GeneralUtils;
import com.ece1778.footprints.util.OrientationUtils;
import com.ece1778.footprints.util.fullscreenUtil.SystemUiHider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {

    private static final String TAG = MapsActivity.class.getName();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private float mid=(float)0.5;

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 5000;
    private static final boolean TOGGLE_ON_CLICK = true;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private SystemUiHider mSystemUiHider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Button btn=(Button)findViewById(R.id.settings_btn);
        btn.setPressed(false);

        // Lock the orientation
        OrientationUtils.setOrientationPortrait(this);

        // Set map up to a default location
        setUpMapIfNeeded();

        // Set up location changed listener.
        LocationManager.getManager(this);
        LocationManager.setLocationChangedListener(new LocationManager.LocationChangedListener() {
            @Override
            public void onChanged(Location location) {
                onLocationChanged(location);
            }
        });

        final View controlsView = findViewById(R.id.settingscreen_content_controls);
        final View contentView = findViewById(R.id.mapscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(1);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
     private void delayedHide(int delayMillis) {
         Runnable mHideRunnable = new Runnable() {
             @Override
             public void run() {
                 mSystemUiHider.hide();
             }
         };
         mHideHandler.removeCallbacks(mHideRunnable);
         mHideHandler.postDelayed(mHideRunnable, delayMillis);
     }

    @Override
    protected void onPause() {
        super.onPause();
        LocationManager.getManager(this).onPause();
        mMap.setOnMarkerClickListener(null);
    }
    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.getManager(this).onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationManager.getManager(this).onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationManager.getManager(this).onResume();
        setUpMapIfNeeded();

        if (mMap != null) {
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return onMarkerClickDo(marker);
                }
            });
        }
        // populate the map.
        populateLocations();
        populateMarker();
    }



    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {

        if (BuildConfig.DEBUG) { Log.d(TAG, "Setup Map");}
        Location location= LocationManager.getManager(this).getLocation();
        //TODO: MAKE SURE TO SAVE LAST KNOWN LOCATION in LOCATION MANAGER
        //LatLng curLoc = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng curLoc = new LatLng(43.65,-79.4);


        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 13));
    }

    // On Location updated from the location manager, we need to add that to the database.
    private void onLocationChanged(Location location) {
        if (BuildConfig.DEBUG) { Log.d (TAG, "Location Changed"); }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String timeString = GeneralUtils.timeMilliToString(location.getTime());

        mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(timeString)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot1))
                        .anchor(mid,mid)
        );
        LocationDBManager.getManager(this).addValue(new LocTableEntry(
                timeString,
                GeneralUtils.locationToString(location),
                ""
        ));
    }

    private void populateLocations () {
        if (LocationDBManager.getManager(this).getValuesCount() > 0) {
            if (BuildConfig.DEBUG) { Log.d(TAG, "Populate Old Locations"); }
            ArrayList<LocTableEntry> entries = LocationDBManager.getManager(this).getAllValues();

            for (LocTableEntry entry : entries) {
                String location = entry.getLocation();
                String[] locationParts = location.split(",");
                double latitude = Double.parseDouble(locationParts[0]);
                double longitude = Double.parseDouble(locationParts[1]);
                String titleString = entry.getTimeStamp();
                if (BuildConfig.DEBUG) { Log.d(TAG, locationParts[0]+ "  " +  locationParts[1] ); }
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(titleString)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot1))
                        .anchor(mid,mid)
                );
            }
        }
    }

    private void populateMarker () {
        if (MarkerDBManager.getManager(this).getValuesCount() > 0) {
            if (BuildConfig.DEBUG) { Log.d(TAG, "Populate Old Markers"); }
            ArrayList<MarkerTableEntry> entries = MarkerDBManager.getManager(this).getAllValues();

            for (MarkerTableEntry entry : entries) {
                String location = entry.getLocation();

                if (BuildConfig.DEBUG) { Log.d(TAG, location ); }
                String[] locationParts = location.split(",");
                double latitude = Double.parseDouble(locationParts[0]);
                double longitude = Double.parseDouble(locationParts[1]);
                String titleString = entry.getTime();
                if (BuildConfig.DEBUG) { Log.d(TAG, locationParts[0]+ "  " +  locationParts[1] ); }
                // TODO: Need to add and save the marker.  Otherwise no other way to add info to marker
                mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(titleString)

                );
            }
        }
    }

    public void goToSettings(View v){
        if (TOGGLE_ON_CLICK) {
            mSystemUiHider.toggle();
        } else {
            mSystemUiHider.show();
        }

    }


    public void loadCamera (View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void addMarker (View v) {
        Location location= LocationManager.getManager(this).getLocation();
        // Add Marker without picture.
        if (location != null) {

            String timeString = GeneralUtils.timeMilliToString(location.getTime());

            if (BuildConfig.DEBUG) { Log.d(TAG, "Add Marker " +
                    GeneralUtils.locationToString(location)); }

            Intent i = new Intent(this, AddMarkerActivity.class);
            i.putExtra(AddMarkerActivity.LOCATION_KEY, GeneralUtils.locationToString(location));
            i.putExtra(AddMarkerActivity.TIMESTAMP_KEY, timeString);

            startActivity(i);

        }
    }

    public void loadAudioRecorder (View v) {
        //Intent i = new Intent(this, AudioRecordActivity.class);
        //startActivity(i);
    }

    public boolean onMarkerClickDo (Marker marker) {
        // Marker was clicked.  Determine the marker info and pull the relevant information
        return true;
    }
}