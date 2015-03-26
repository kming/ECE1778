package com.ece1778.footprints.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

import java.io.File;
import java.util.ArrayList;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSystemUiHider.hide();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        }
        // populate the map.
        populateLocations();
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

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Setup Map");
        }
        //TODO: MAKE SURE TO SAVE LAST KNOWN LOCATION in LOCATION MANAGER
        //LatLng curLoc = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng curLoc = new LatLng(43.65, -79.4);

        mMap.setInfoWindowAdapter(new InfoWindowAdapter(getLayoutInflater()));
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 13));
    }

    // On Location updated from the location manager, we need to add that to the database.
    private void onLocationChanged(Location location) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Location Changed");
        }

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
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Populate Old Locations");
            }
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

                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, location);
                        }
                        String[] locationParts = location.split(",");
                        double latitude = Double.parseDouble(locationParts[0]);
                        double longitude = Double.parseDouble(locationParts[1]);
                        String titleString = entry.getTitle();
                        String snippetString = entry.getNote();
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, locationParts[0] + "  " + locationParts[1]);
                        }
                        // TODO: Need to add and save the marker.  Otherwise no other way to add info to marker
                        mk = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "LoadCamera button Clicked" + location);
        }
        if (location != null) {

            String timeString = GeneralUtils.timeMilliToString(location.getTime());

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Add Marker " +
                        GeneralUtils.locationToString(location));
            }

            Intent i = new Intent(this, CameraActivity.class);
            i.putExtra(CameraActivity.LOCATION_KEY, GeneralUtils.locationToString(location));
            i.putExtra(CameraActivity.TIMESTAMP_KEY, timeString);

            startActivity(i);
        }
    }


    public void addMarker(View v) {
        Location location = MotionDetectionLocationService.getLocation();
        // Add Marker without picture.
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "AddMarker button Clicked" + location);
        }
        if (location != null) {

            String timeString = GeneralUtils.timeMilliToString(location.getTime());

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Add Marker " +
                        GeneralUtils.locationToString(location));
            }

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
                    .strokeWidth(0)
                            //.strokeColor(Color.RED)
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


                for (int i = -3; i < 4; i++) {
                    int a = (3 - Math.abs(i)) * 2;

                    for (int j = -a; j < a + 1; j++) {
                        tempMidPt = new LatLng(latitude + i * 0.001, longitude + j * 0.001);
                        if (holesMidPt.contains(tempMidPt) == false) {
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
            populateLocations();
            populateMarker();
        }

    }

    private void onMarkerAdapterClicked(LatLng coordinates) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 13));

        LatLng coord = new LatLng(coordinates.latitude + 0.012, coordinates.longitude - 0.001);
        if (markerList.get(coord) != null) {
            markerList.get(coord).showInfoWindow();
        }
    }

}