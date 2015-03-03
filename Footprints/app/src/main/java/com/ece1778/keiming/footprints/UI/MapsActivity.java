package com.ece1778.keiming.footprints.UI;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ece1778.keiming.footprints.BuildConfig;
import com.ece1778.keiming.footprints.Classes.LocTableEntry;
import com.ece1778.keiming.footprints.Managers.LocationDBManager;
import com.ece1778.keiming.footprints.Managers.LocationManager;
import com.ece1778.keiming.footprints.R;
import com.ece1778.keiming.footprints.Utils.GeneralUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Timestamp;

public class MapsActivity extends FragmentActivity {

    private static final String TAG = MapsActivity.class.getName();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Set up location changed listener.
        LocationManager.getManager(this);
        LocationManager.setLocationChangedListener(new LocationManager.LocationChangedListener() {
            @Override
            public void onChanged(Location location, Timestamp timestamp) {
                onLocationChanged(location, timestamp);
            }
        });

        // Set map up to a default location
        setUpMapIfNeeded();
    }
    @Override
    protected void onPause() {
        super.onPause();
        LocationManager.getManager(this).onPause();
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
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(43.65,-79.4)).title("Toronto"));

        LatLng curLoc = new LatLng( 43.7,-79.4);

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 13));
    }

    // On Location updated from the location manager, we need to add that to the database.
    private void onLocationChanged(Location location, Timestamp timestamp) {
        if (BuildConfig.DEBUG) { Log.d (TAG, "Location Changed"); }
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("#1"));

        LocationDBManager.getManager(this).addValue(new LocTableEntry(
            timestamp.toString(),
            GeneralUtils.locationToString(location),
            ""
        ));
    }

    public void goToSettings(View v){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
    }

}
