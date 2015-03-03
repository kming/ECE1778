package com.ece1778.keiming.footprints.UI;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ece1778.keiming.footprints.Managers.CameraManager;
import com.ece1778.keiming.footprints.Managers.LocationManager;
import com.ece1778.keiming.footprints.R;
import com.ece1778.keiming.footprints.Utils.GeneralUtils;

public class CameraActivity extends ActionBarActivity {

    private String TAG = "CameraActivity";
    private CameraManager cameraManager;
    private Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        showActionBarTemp();
        // Use another thread to run
        final Runnable r = new Runnable() {
            public void run() {
                // Setup one time singleton classes
                // Setup Database on new App.  All adding and removing must go through the gallery Adapter
                //DatabaseHandler.initHandler(mContext);
                // Setup the Gallery
                //GalleryEntryAdapter.initHandler(mContext);

                // Create Location Handler
                LocationManager.initHandler(mContext);
                // Initialize the Camera
                cameraManager = new CameraManager(mContext, R.id.camera_preview);
                cameraManager.setCameraInterface(new CameraManager.CameraInterface() {
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
            };
        };
        r.run();


    }

    @Override
    public void onResume() {
        super.onResume();
        // Use another thread to run
        final Runnable r = new Runnable() {
            public void run() {
                LocationManager.onResume();
                cameraManager.onResume();
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
                LocationManager.onPause();
                cameraManager.onPause();
            }
        };
        r.run();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationManager.onStop();
    }

    public void takePicture() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Take Picture
                cameraManager.takePicture();
            }
        }, 1000);
    }

    private void onPictureCallback(Uri uri) {
        final String location = LocationManager.getHandler().getLocationString();
        final String dateTime = GeneralUtils.getDateTimeString();
        //saveToGallery(location, uri.toString(), dateTime);
    }

   /* private void saveToGallery (final String location, final String uri, final String dateTimeNote) {
        final Runnable r = new Runnable() {
            public void run() {
                // Saves to Gallery
                GalleryEntryAdapter.getGalleryHandler().add(
                        new TableEntry(location, uri, dateTimeNote)
                );
            }
        };
        r.run();
    }*/
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
