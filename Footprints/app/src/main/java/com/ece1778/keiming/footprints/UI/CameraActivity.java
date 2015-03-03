package com.ece1778.keiming.footprints.UI;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ece1778.keiming.footprints.Managers.CameraManager;
import com.ece1778.keiming.footprints.Managers.LocationManager;
import com.ece1778.keiming.footprints.R;
import com.ece1778.keiming.footprints.Utils.GeneralUtils;

public class CameraActivity extends ActionBarActivity {

    private String TAG = CameraActivity.class.getName();
    private CameraManager cameraManager;
    private Context mContext = this;
    private boolean mEnabled = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraManager = new CameraManager(mContext, R.id.camera_preview);
        cameraManager.setCameraInterface(new CameraManager.CameraInterface() {
            @Override
            public void onPictureTaken(Uri uri) {
                Log.d(TAG, "onPictureTaken");
                onPictureCallback(uri);
            }
            @Override
            public void onClickListener() {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        cameraManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraManager.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void takePicture(View v) {
        cameraManager.takePicture();
    }

    private void onPictureCallback(Uri uri) {
        //final String location = LocationManager.getHandler().getLocationString();
        //final String dateTime = GeneralUtils.getDateTimeString();
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

}
