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
import com.ece1778.keiming.footprints.Utils.OrientationUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Timestamp;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {

    private static final String TAG = MapsActivity.class.getName();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private float mid=(float)0.5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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

    public void loadCamera (View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
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
        //Location location= LocationManager.getManager(this).getLocation();
        //LatLng curLoc = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng curLoc = new LatLng(43.65,-79.4);

        // populate the map.
        populateLocations();

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
                        .anchor(0,mid)
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
                        .anchor(0,mid)
                );
            }

        }
    }

    private void populateMarker () {

    }

    public void goToSettings(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}