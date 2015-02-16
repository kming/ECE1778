package com.ece1778.keiming.assignment3.UI;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.ece1778.keiming.assignment3.BackendHandlers.CameraHandler;
import com.ece1778.keiming.assignment3.BackendHandlers.Database.DatabaseHandler;
import com.ece1778.keiming.assignment3.BackendHandlers.Database.TableEntry;
import com.ece1778.keiming.assignment3.BackendHandlers.LocationHandler;
import com.ece1778.keiming.assignment3.BackendHandlers.MotionDetectionHandler;
import com.ece1778.keiming.assignment3.R;
import com.ece1778.keiming.assignment3.Utils.GeneralUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Calendar;

// Two main activities
// 1) Camera Preview
// 2) Gallery View
// Use a sql database to keep track of the location and coordinates of the photo


public class MainActivity extends ActionBarActivity {
    private String TAG = "MainActivity";
    private CameraHandler cameraHandler;
    private Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showActionBarTemp();
        // Use another thread to run
        final Runnable r = new Runnable() {
            public void run() {
                // Setup one time singleton classes
                // Setup Database on new App.  All adding and removing must go through the gallery Adapter
                DatabaseHandler.initHandler(mContext);
                // Setup the Gallery
                GalleryEntryAdapter.initHandler(mContext);
                // Setup the Shake Monitor Handler
                MotionDetectionHandler.initHandler(mContext);
                MotionDetectionHandler.setShakeListener(new MotionDetectionHandler.ShakeListener() {
                    @Override
                    public void onShake() {
                        onShakeAction();
                    }
                });
                // Create Location Handler
                LocationHandler.initHandler(mContext);
                // Initialize the Camera
                cameraHandler = new CameraHandler(mContext, R.id.camera_preview);
                cameraHandler.setCameraInterface(new CameraHandler.CameraInterface() {
                    @Override
                    public void onPictureTaken(Uri uri) {
                        Log.d(TAG, "onPictureTaken");
                        onPictureCallback(uri);
                    }

                    @Override
                    public void onClickListener() {
                        showActionBarTemp();
                    }
                });
            }
        };
        r.run();


    }

    @Override
    public void onResume() {
        super.onResume();
        // Use another thread to run
        final Runnable r = new Runnable() {
            public void run() {
                LocationHandler.onResume();
                MotionDetectionHandler.onResume();
                cameraHandler.onResume();
            }
        };
        r.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Use another thread to run
        final Runnable r = new Runnable() {
            public void run() {
                LocationHandler.onPause();
                MotionDetectionHandler.onPause();
                cameraHandler.onPause();
            }
        };
        r.run();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationHandler.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationHandler.onStop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_gallery) {
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onShakeAction() {
        MotionDetectionHandler.onPause();
        // Shake Detected, trigger capture of event.  For now wait 1 seconds, then save location
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Take Picture
                cameraHandler.takePicture();
                // Resume detection
                MotionDetectionHandler.onResume();
            }
        }, 1000);
    }

    private void onPictureCallback(Uri uri) {
        final String location = LocationHandler.getHandler().getLocationString();
        final String dateTime = GeneralUtils.getDateTimeString();
        saveToGallery(location, uri.toString(), dateTime);
    }

    private void saveToGallery (final String location, final String uri, final String dateTimeNote) {
        final Runnable r = new Runnable() {
            public void run() {
                // Saves to Gallery
                GalleryEntryAdapter.getGalleryHandler().add(
                        new TableEntry(location, uri, dateTimeNote)
                );
            }
        };
        r.run();
    }
    private void showActionBarTemp () {
        final ActionBar actionBar = getSupportActionBar();
        // show ActionBar
        actionBar.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // hide the actionbar after initially showing it
                actionBar.show();
            }
        }, 2000);
    }
}

