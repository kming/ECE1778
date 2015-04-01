package com.ece1778.footprints.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ece1778.footprints.R;
import com.ece1778.footprints.database.NeighbourhoodDBManager;
import com.ece1778.footprints.database.NeighbourhoodTableEntry;
import com.ece1778.footprints.util.OrientationUtils;
import com.ece1778.footprints.util.fullscreenUtil.SystemUiHider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static com.ece1778.footprints.util.FileUtils.readJSON;
import static com.ece1778.footprints.util.FileUtils.readLinks;
import static com.ece1778.footprints.util.GeneralUtils.performOnBackgroundThread;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LauncherActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 5000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    /**
     * Class Strings
     */
    private static final String ENABLE_GPS_STRING = "Enable GPS";
    private static final String LOGON_STRING = "Start Exploring";
    private static final String TAG = LauncherActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;
        // Read in the one time loading the neighbourhoods and creeks
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog pdia;

            @Override
            protected void onPreExecute(){
                pdia = new ProgressDialog(context);
                pdia.setMessage("Loading...");
                pdia.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                //TODO: Uses the count to see if its updated.  Ideally, we want to see when its updated
                if (NeighbourhoodDBManager.getManager(getApplicationContext()).getValuesCount() == 0) {
                    // Read in a neighbourhood JSON file
                    try {
                        InputStream inputStreamLink = getAssets().open("neighbourhoods_link.csv");
                        ArrayMap<String, String> links = new ArrayMap<String, String>();
                        for (String line : readLinks(inputStreamLink)) {
                            String[] subString = line.split(",");
                            links.put(subString[0], subString[1]);
                        }
                        inputStreamLink.close();
                        InputStream inputStream = getAssets().open("neighbourhoods.txt");
                        try {
                            JSONArray neighbourhoods = readJSON(inputStream).getJSONArray("features");
                            for (int i = 0; i < neighbourhoods.length(); i++) {
                                JSONObject neighbourhood = neighbourhoods.getJSONObject(i);
                                String name = neighbourhood.getJSONObject("properties").getString("AREA_NAME");
                                String coords = neighbourhood.getJSONObject("geometry").getString("coordinates");
                                // Store the name and the coords and the status into a database.
                                NeighbourhoodDBManager.getManager(getApplicationContext()).addValue(new NeighbourhoodTableEntry(
                                        name,
                                        coords,
                                        NeighbourhoodTableEntry.SHOW,
                                        links.get(name)
                                ));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                pdia.dismiss();
                return;
            }
        }.execute();



        setContentView(R.layout.activity_launcher);

        // Lock the orientation
        OrientationUtils.setOrientationPortrait(this);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

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

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });
        checkGPS();
    }

    @Override
    public void onResume () {
        super.onResume();
        checkGPS(); // check GPS when user comes back to app.
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
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
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * On Click Functions for this activity
     */
    public void enableButton (View v) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPS = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
        if (isGPS) {
            Toast.makeText(this, "Logon", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, MapsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
        }
    }

    private void checkGPS () {
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        // Also checks gps.  If it is enabled, then we allow login.
        Button enableButton = (Button) findViewById(R.id.enable_button);
        enableButton.setOnTouchListener(mDelayHideTouchListener);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPS = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
        if (isGPS) {
            enableButton.setText(LOGON_STRING);
        } else {
            enableButton.setText(ENABLE_GPS_STRING);
            Toast.makeText(this, "Please Enable GPS Settings First", Toast.LENGTH_LONG).show();
        }
    }
}
