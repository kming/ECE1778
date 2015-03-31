package com.ece1778.footprints.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.R;
import com.ece1778.footprints.database.LocTableEntry;
import com.ece1778.footprints.database.LocationDBManager;
import com.ece1778.footprints.database.MarkerDBManager;
import com.ece1778.footprints.database.MarkerTableEntry;
import com.ece1778.footprints.manager.AudioRecordManager;
import com.ece1778.footprints.manager.InfoWindowAdapter;
import com.ece1778.footprints.manager.MarkerScrollAdapter;
import com.ece1778.footprints.manager.MotionDetectionLocationService;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private static final String TAG = MapsActivity.class.getName();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private float mid = (float) 0.5;

    //SystemUIHider.FLAG_HIDE_NAVIGATION
    private static final int HIDER_FLAGS = 0;
    private SystemUiHider mSystemUiHider;
    private long mDownTime = 0;
    private long mUpTime = 0;
    private Uri mRecording = null;

    public static final String LAST_SAVED_POINT = "LastSavedPoint";
    public int saveLastPointProcessed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //restore preferences

        SharedPreferences settings = getSharedPreferences(LAST_SAVED_POINT, 0);
        saveLastPointProcessed= settings.getInt("lastPoint",0);

        Button btn = (Button) findViewById(R.id.settings_btn);
        btn.setPressed(false);

        RelativeLayout recomView = (RelativeLayout) findViewById(R.id.recommendation_view);
        recomView.setVisibility(View.GONE);

        // Lock the orientation
        OrientationUtils.setOrientationPortrait(this);

        // Set map up to a default location
        setUpMapIfNeeded();

        // Set up location changed listener.
        CheckBox trackOnOff = (CheckBox) findViewById(R.id.toggle_tracking);
        trackOnOff.setChecked(true);
        MotionDetectionLocationService.setLocationChangedListener(
                new MotionDetectionLocationService.LocationChangedListener() {
                    @Override
                    public void onChanged(Location location) {
                        onLocationChanged(location);
                    }
                });
        Intent intent = new Intent(this, MotionDetectionLocationService.class);
        startService(intent);

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

                    }
                });

        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        recList.setLayoutManager(llm);

        // Initialize the Recorder Class
        ImageButton startAudio = (ImageButton) findViewById(R.id.record_btn);
        startAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onAudioTouch(v,event);
            }
        });



    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setMessage("Exit Application")
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                  dialog.dismiss();
                  Intent intent = new Intent(Intent.ACTION_MAIN);
                  intent.addCategory(Intent.CATEGORY_HOME);
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  startActivity(intent);
              }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        // Create the AlertDialog object and show it
        db.create().show();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        mSystemUiHider.hide();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mSystemUiHider.hide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.setOnMarkerClickListener(null);
        mMap.setOnInfoWindowClickListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSystemUiHider.hide();
    }

    @Override
    protected void onStop() {

        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(LAST_SAVED_POINT, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("lastPoint", saveLastPointProcessed);

        // Commit the edits!
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSystemUiHider.hide();
        setUpMapIfNeeded();
        if (mMap != null) {
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return onMarkerClickDo(marker);
                }
            });
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    onInfoWindowClickDo(marker);
                    return;
                }
            });
        }
        // populate the map.
        //populateLocations();
        populateLocationsFiltered();
        populateMarker();
        mSystemUiHider.hide();
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
        //TODO: MAKE SURE TO SAVE LAST KNOWN LOCATION in LOCATION MANAGER
        //LatLng curLoc = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng curLoc = new LatLng(43.65, -79.4);

        mMap.setInfoWindowAdapter(new InfoWindowAdapter(getLayoutInflater()));
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
                        .anchor(mid, mid)
        );
        LocationDBManager.getManager(this).addValue(new LocTableEntry(
                timeString,
                GeneralUtils.locationToString(location),
                ""
        ));
    }

    private void populateLocations() {
        if (LocationDBManager.getManager(this).getValuesCount() > 0) {
            if (BuildConfig.DEBUG) { Log.d(TAG, "Populate Old Locations"); }
            new AsyncTask<Void, Void, ArrayList<LocTableEntry>>() {
                @Override
                protected ArrayList<LocTableEntry> doInBackground(Void... params) {
                    return LocationDBManager.getManager(getApplicationContext()).getAllValues();
                }

                @Override
                protected void onPostExecute(ArrayList<LocTableEntry> entries) {
                    for (LocTableEntry entry : entries) {
                        String location = entry.getLocation();
                        String[] locationParts = location.split(",");
                        double latitude = Double.parseDouble(locationParts[0]);
                        double longitude = Double.parseDouble(locationParts[1]);
                        String titleString = entry.getTimeStamp();
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, locationParts[0] + "  " + locationParts[1]);
                        }
                        mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                                //.title(titleString)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot1))
                                        .anchor(mid, mid)
                        );
                    }
                    return;
                }
            }.execute();
        }
    }

    private void populateLocationsFiltered () {
        if (LocationDBManager.getManager(this).getValuesCount() > 0) {
            double tol=0.0005;
            ArrayList<LocTableEntry> entries= poly_decimate(tol);
            LocationDBManager.getManager(this).deleteDatabase();
            ArrayList<LatLng> vertices=new ArrayList<LatLng>();
                    for (LocTableEntry entry : entries) {
                        String location = entry.getLocation();
                        String[] locationParts = location.split(",");
                        double latitude = Double.parseDouble(locationParts[0]);
                        double longitude = Double.parseDouble(locationParts[1]);
                        String titleString = entry.getTimeStamp();
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, locationParts[0] + "  " + locationParts[1]);
                        }
                        mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                                //.title(titleString)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot1))
                                        .anchor(mid, mid)
                        );
                        vertices.add(new LatLng(latitude,longitude));
                        LocationDBManager.getManager(this).addValue(entry);
                    }
            mMap.addPolyline(new PolylineOptions()
                    .addAll(vertices)
                    .color(0xaaFFDE00)
                    .width(20));
        }
    }

    private HashMap<LatLng, Marker> markerList = new HashMap<LatLng, Marker>();

    private void populateMarker() {
        if (MarkerDBManager.getManager(this).getValuesCount() > 0) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Populate Old Markers");
            }
            new AsyncTask<Void, Void, ArrayList<MarkerTableEntry>>() {
                @Override
                protected ArrayList<MarkerTableEntry> doInBackground(Void... params) {
                    return MarkerDBManager.getManager(getApplicationContext()).getAllValues();
                }

                @Override
                protected void onPostExecute(ArrayList<MarkerTableEntry> entries) {
                    Marker mk;
                    for (MarkerTableEntry entry : entries) {
                        String location = entry.getLocation();
                        String titleString = entry.getTitle();
                        String snippetString = entry.getNote()+ " .,.," + entry.getPicture()+" .,.,"+entry.getAudio();

                        // TODO: Need to add and save the marker.  Otherwise no other way to add info to marker
                        mk = mMap.addMarker(new MarkerOptions()
                                        .position(GeneralUtils.stringToLocation(location))
                                        .title(titleString)
                                        .snippet(snippetString)
                        );
                        markerList.put(mk.getPosition(), mk);
                    }

                    return;
                }

            }.execute();
        }
    }

    public void goToSettings(View v) {

        ToggleButton btn = (ToggleButton) findViewById(R.id.settings_btn);

        if (btn.isChecked()) {
            mSystemUiHider.show();
        } else {
            mSystemUiHider.hide();
        }
    }

    public void loadCamera(View view) {
        Location location = MotionDetectionLocationService.getLocation();
        // Add Marker without picture.
        if (BuildConfig.DEBUG) { Log.d(TAG, "LoadCamera button Clicked" +location); }
        if (location != null) {

            String timeString = GeneralUtils.timeMilliToString(location.getTime());

            if (BuildConfig.DEBUG) { Log.d(TAG, "Add Marker " +
                    GeneralUtils.locationToString(location)); }

            Intent i = new Intent(this, CameraActivity.class);
            i.putExtra(CameraActivity.LOCATION_KEY, GeneralUtils.locationToString(location));
            i.putExtra(CameraActivity.TIMESTAMP_KEY, timeString);

            startActivity(i);
        }
    }


    public void addMarker(View v) {
        Location location = MotionDetectionLocationService.getLocation();
        // Add Marker without picture.
        if (BuildConfig.DEBUG) { Log.d(TAG, "AddMarker button Clicked" +location); }
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

    public Boolean onAudioTouch(View v, MotionEvent event) {
        Location location = MotionDetectionLocationService.getLocation();
        // Add Marker without picture.
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Record Audio button Event" + location);
        }
        if (location != null) {

            String timeString = GeneralUtils.timeMilliToString(location.getTime());

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    // Stop the Recording
                    mUpTime = System.currentTimeMillis();
                    try {
                        AudioRecordManager.getManager().stop();
                    } catch (RuntimeException e) {
                        // Stop called too early, no recording done. we need to catch the exception
                        mRecording = null;
                        AudioRecordManager.getManager().reset();
                    }
                    if ((mUpTime - mDownTime) < 500) {
                        // It is a click. Either recording started or not.
                        Toast.makeText(
                                this,
                                "Long Press to record",
                                Toast.LENGTH_SHORT
                        ).show();
                        if (mRecording != null) {
                            new File(mRecording.getPath()).delete();
                        }
                    } else {
                        Toast.makeText(
                                this,
                                "Stopped Recording",
                                Toast.LENGTH_SHORT
                        ).show();
                        Intent i = new Intent (this, AddMarkerActivity.class);
                        i.putExtra(AddMarkerActivity.LOCATION_KEY, GeneralUtils.locationToString(location));
                        i.putExtra(AddMarkerActivity.TIMESTAMP_KEY, timeString);
                        i.putExtra(AddMarkerActivity.AUDIO_URI_KEY, mRecording.toString());
                        startActivity(i);

                    }

                    break;
                case MotionEvent.ACTION_DOWN:
                    // Start the Recording
                    mDownTime = System.currentTimeMillis();
                    mRecording = AudioRecordManager.getManager().start();
                    break;
                default:

                    break;

            }
        }
        return false;
    }

    private List<MarkerTableEntry> createList(int size) {

        List<MarkerTableEntry> savedEntries = MarkerDBManager.getManager(this).getAllValues();
        List<MarkerTableEntry> result = new ArrayList<MarkerTableEntry>();

        for (MarkerTableEntry entry : savedEntries) {
            MarkerTableEntry marker = new MarkerTableEntry();
            marker.setTitle(entry.getTitle());
            marker.setNote(entry.getNote());
            marker.setPicture(entry.getPicture());
            marker.setLocation(entry.getLocation());

            result.add(marker);

        }

        return result;
    }

    public void toggleTracking (View v) {
        CheckBox trackOnOff = (CheckBox) findViewById(R.id.toggle_tracking);
        if (trackOnOff.isChecked()) {
            // Turn on Tracking
            Intent intent = new Intent(this, MotionDetectionLocationService.class);
            startService(intent);
        } else {
            // Turn off location tracking services
            Intent intent = new Intent(this, MotionDetectionLocationService.class);
            stopService(intent);
        }
    }
    public void showRecommendations(View v) {

        CheckBox recomOnOff = (CheckBox) findViewById(R.id.view_recom_btn);
        RelativeLayout recomView = (RelativeLayout) findViewById(R.id.recommendation_view);

        if (recomOnOff.isChecked()) {

            if (MarkerDBManager.getManager(this).getValuesCount() > 0) {
                //show recommendation box

                RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);

                MarkerScrollAdapter msa = new MarkerScrollAdapter(createList(5));
                msa.setOnClickedListener(new MarkerScrollAdapter.onClickedListener() {
                    @Override
                    public void onChanged(LatLng coordinates) {
                        onMarkerAdapterClicked(coordinates);
                    }
                });
                recList.setAdapter(msa);

                recomView.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(this, "No markers yet", Toast.LENGTH_SHORT).show();
                recomOnOff.setChecked(false);
            }
        } else {
            recomView.setVisibility(View.GONE);
        }
    }

    public boolean onMarkerClickDo(Marker marker) {
        if (marker.getTitle() != null) {
            marker.showInfoWindow();
        }
        return true;
    }

    public void onInfoWindowClickDo(Marker marker) {
        if (marker.getTitle() != null) {

        }
        return;
    }

    public void generateFog(View v) {
        CheckBox fogOnOff = (CheckBox) findViewById(R.id.fog_btn);
        float delta = 0.1f;

        if (fogOnOff.isChecked()) {
            Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(90, -180),
                        new LatLng(-90 + delta, -180 + delta),
                        new LatLng(-90 + delta, 0),
                        new LatLng(-90 + delta, 180 - delta),
                        new LatLng(0, 180 - delta),
                        new LatLng(90 - delta, 180 - delta),
                        new LatLng(90 - delta, 0),
                        new LatLng(90 - delta, -180 + delta),
                        new LatLng(0, -180 + delta))
                //.strokeWidth(0)
                .strokeColor(Color.RED)
                .fillColor(Color.WHITE));

            ArrayList<LocTableEntry> entries = LocationDBManager.getManager(this).getAllValues();
            ArrayList<ArrayList<LatLng>> holes = new ArrayList<ArrayList<LatLng>>();
            ArrayList<LatLng> holesMidPt = new ArrayList<LatLng>();
            ArrayList<LatLng> hole;

            LatLng tempMidPt;
            double latitude;
            double longitude;

            for (LocTableEntry entry : entries) {
                String location = entry.getLocation();
                String[] locationParts = location.split(",");
                latitude = Double.parseDouble(locationParts[0]) * 1000;
                longitude = Double.parseDouble(locationParts[1]) * 1000;
                latitude = Math.round(latitude);
                longitude = Math.round(longitude);
                latitude = latitude / 1000;
                longitude = longitude / 1000;

                for(int i=-3;i<4;i++){
                    int a= (3-Math.abs(i))*2;

                    for(int j=-a;j<a+1;j++){
                        tempMidPt=new LatLng(latitude+i*0.001,longitude+j*0.001);
                        if (holesMidPt.contains(tempMidPt)==false) {
                            holesMidPt.add(tempMidPt);
                        }
                    }
                }
            }

            for (LatLng entry2 : holesMidPt) {

                latitude = entry2.latitude;
                longitude = entry2.longitude;

                hole = new ArrayList<LatLng>();
                hole.add(new LatLng(latitude + 0.0005, longitude - 0.0005));
                hole.add(new LatLng(latitude + 0.0005, longitude + 0.00049));
                hole.add(new LatLng(latitude - 0.00049, longitude + 0.00049));
                hole.add(new LatLng(latitude - 0.00049, longitude - 0.0005));
                hole.add(new LatLng(latitude + 0.0005, longitude - 0.0005));

                holes.add(hole);
            }

            if (holes.size() >= 1) {
                polygon.setHoles(holes);
            }

        } else {
            mMap.clear();
            //populateLocations();
            populateLocationsFiltered();
            populateMarker();
        }

    }

    private void onMarkerAdapterClicked(LatLng coordinates) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 13));

        LatLng coord=new LatLng(coordinates.latitude+0.012,coordinates.longitude-0.001);

        if (markerList.get(coord)!=null) {
            markerList.get(coord).showInfoWindow();
        }
    }

    // dot product (3D) which allows vector operations in arguments
    /*#define dot(u,v)  ((u).x * (v).x + (u).y * (v).y + (u).z * (v).z)
            #define norm2(v)  dot(v,v)         // norm2 = squared length of vector
            #define norm(v)   sqrt(norm2(v))   // norm = length of vector
            #define d2(u,v)   norm2(u-v)       // distance squared = norm2 of difference
            #define d(u,v)    norm(u-v)        // distance = norm of difference*/
    private double dot(LatLng u, LatLng v){
        double dotProduct=u.latitude*v.latitude+u.longitude*v.longitude;
        return dotProduct;
    }

    private double norm2(LatLng v){
        double ans=dot(v,v);
        return ans;
    }

    private double norm(LatLng v){
        return Math.sqrt(norm2(v));
    }

    private LatLng UMinusV(LatLng u, LatLng v){
        double lat=u.latitude-v.latitude;
        double lng=u.longitude-v.longitude;
        LatLng difference=new LatLng(lat,lng);
        return difference;
    }

    private double d2(LatLng u, LatLng v){
        LatLng w=UMinusV(u,v);
        double ans=norm2(w);
        return ans;
    }

    private double d(LatLng u, LatLng v){
        LatLng w=UMinusV(u,v);
        double ans=norm(w);
        return ans;
    }

    public int[]  mk;
    // poly_decimate(): - remove vertices to get a smaller approximate polygon
//    Input:  tol = approximation tolerance
//            V[] = polyline array of vertex points
//            n   = the number of points in V[]
//    Output: sV[]= reduced polyline vertexes (max is n)
//    Return: m   = the number of points in sV[]*/
    private ArrayList<LocTableEntry> poly_decimate(double tol)
    {
        ArrayList<LocTableEntry> V = LocationDBManager.getManager(this).getAllValues();
        int n=LocationDBManager.getManager(this).getValuesCount();
        ArrayList<LocTableEntry> sV = new ArrayList<LocTableEntry>();
        int    i, k, m, pv;             // misc counters
        double  tol2 = tol * tol;        // tolerance squared
        ArrayList<LocTableEntry> vt = new ArrayList<LocTableEntry>();       // vertex buffer
        mk = new int[n];   // marker  buffer

        // STAGE 1.  Vertex Reduction within tolerance of  prior vertex cluster
        vt.add(0,V.get(0));               // start at the beginning
        for (i=k=1, pv=0; i<n; i++) {
            if(i<saveLastPointProcessed){
                if (d2(GeneralUtils.stringToLocation(V.get(i).getLocation()),
                        GeneralUtils.stringToLocation(V.get(pv).getLocation())) < tol2) {
                    continue;
                }

                if (i<n-1) {
                    double distance1 = d2(GeneralUtils.stringToLocation(V.get(i).getLocation()),
                            GeneralUtils.stringToLocation(V.get(i - 1).getLocation()));

                    double distance2 = d2(GeneralUtils.stringToLocation(V.get(i + 1).getLocation()),
                            GeneralUtils.stringToLocation(V.get(i - 1).getLocation()));

                    if (distance1 > distance2 / 1.5) {
                        continue;
                    }
                }
            }
            vt.add(k,V.get(i));
            k++;
            pv = i;
        }
        if (pv < n-1) {
            if (BuildConfig.DEBUG) { Log.d(TAG, "vortex elimination "+V.get(n-1).getLocation()+" "+n+" "+k);}
            vt.add(k, V.get(n-1));       // finish at the end
            k++;
        }

        // STAGE 2.  Douglas-Peucker polyline reduction
        mk[0] = mk[k-1] = 1;       //  mark the first and last vertexes
        poly_decimateDP(tol, vt, saveLastPointProcessed, k-1);

        // copy marked vertices to the reduced polyline
        for (i=m=0; i<k; i++) {
            if (mk[i]!=0 || i< saveLastPointProcessed) {
                sV.add(m, vt.get(i));
                m++;
            }
        }
        saveLastPointProcessed=k-1;
        //vt.clear();
        return sV;         //  m vertices in reduced polyline
    }


    // poly_decimateDP():
//  This is the Douglas-Peucker recursive reduction routine
//  It marks vertexes that are part of the reduced polyline
//  for approximating the polyline subchain v[j] to v[k].
//    Input:  tol  = approximation tolerance
//            v[]  = polyline array of vertex points
//            j,k  = indices for the subchain v[j] to v[k]
//    Output: mk[] = array of markers matching vertex array v[]
    private void poly_decimateDP( double tol, ArrayList<LocTableEntry> v, int j, int k)
    {
        if (k <= j+1) // there is nothing to decimate
            return;

        // check for adequate approximation by segment S from v[j] to v[k]
        int     maxi = j;           // index of vertex farthest from S
        double   maxd2 = 0;          // distance squared of farthest vertex
        double tol2 = tol * tol;   // tolerance squared
        LatLng S0 = GeneralUtils.stringToLocation(v.get(j).getLocation());   //start point of segment
        LatLng S1 = GeneralUtils.stringToLocation(v.get(k).getLocation());   //end point of segment
        LatLng  u = UMinusV(S1,S0);    // segment direction vector
        double  cu = dot(u,u);      // segment length squared

        // test each vertex v[i] for max distance from S
        // compute using the Algorithm dist_Point_to_Segment()
        // Note: this works in any dimension (2D, 3D, ...)
        LatLng  w;
        LatLng   Pb;                 // base of perpendicular from v[i] to S
        double  b, cw, dv2;         // dv2 = distance v[i] to S squared

        for (int i=j+1; i<k; i++)
        {
            // compute distance squared
            w = UMinusV(GeneralUtils.stringToLocation(v.get(i).getLocation()),S0);
            cw = dot(w,u);
            if ( cw <= 0 )
                dv2 =d2(GeneralUtils.stringToLocation(v.get(i).getLocation()), S0);
            else if ( cu <= cw )
                dv2 =d2(GeneralUtils.stringToLocation(v.get(i).getLocation()), S1);
            else {
                b = cw / cu;
                Pb = UMinusV(S0,new LatLng(-b*u.latitude,-b*u.longitude));
                dv2 =d2(GeneralUtils.stringToLocation(v.get(i).getLocation()), Pb);
            }
            // test with current max distance  squared
            if (dv2 <= maxd2)
                continue;
            // v[i] is a new max vertex
            maxi = i;
            maxd2 = dv2;
        }
        if (maxd2 > tol2)         // error is worse than the tolerance
        {
            // split the polyline at the farthest  vertex from S
            mk[maxi] = 1;       // mark v[maxi] for the reduced polyline
            // recursively decimate the two subpolylines at v[maxi]
            poly_decimateDP(tol, v, j, maxi);  // polyline v[j] to v[maxi]
            poly_decimateDP(tol, v, maxi, k);  // polyline v[maxi] to v[k]
        }
        // else the approximation is OK, so ignore intermediate vertexes
        return;
    }

}