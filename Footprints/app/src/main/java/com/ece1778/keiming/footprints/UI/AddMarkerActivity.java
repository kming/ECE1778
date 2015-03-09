package com.ece1778.keiming.footprints.UI;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ece1778.keiming.footprints.BuildConfig;
import com.ece1778.keiming.footprints.Classes.LocTableEntry;
import com.ece1778.keiming.footprints.Classes.MarkerTableEntry;
import com.ece1778.keiming.footprints.Managers.LocationDBManager;
import com.ece1778.keiming.footprints.Managers.LocationManager;
import com.ece1778.keiming.footprints.Managers.MarkerDBManager;
import com.ece1778.keiming.footprints.R;
import com.ece1778.keiming.footprints.Utils.GeneralUtils;

import java.net.URI;

public class AddMarkerActivity extends ActionBarActivity {
    private static final String TAG = AddMarkerActivity.class.getName();
    public static final String PIC_URI_KEY = "pictureUri";
    public static final String AUDIO_URI_KEY = "audioUri";
    public static final String LOCATION_KEY = "location";
    public static final String TIMESTAMP_KEY = "timestamp";

    private String mPictureUri = null;
    private String mAudioUri = null;
    private String mNote= null;
    private String mLocation = null;
    private String mTimestamp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);
        populateElements();
    }

    private void populateElements () {
        Intent i = getIntent();
        mPictureUri = i.getStringExtra(PIC_URI_KEY);
        mAudioUri   = i.getStringExtra(AUDIO_URI_KEY);
        mLocation = i.getStringExtra(LOCATION_KEY);
        mTimestamp = i.getStringExtra(TIMESTAMP_KEY);

        if (BuildConfig.DEBUG) { Log.d(TAG, "picture: " + mPictureUri); }
        if (BuildConfig.DEBUG) { Log.d(TAG, "audio: " + mAudioUri); }
        if (BuildConfig.DEBUG) { Log.d(TAG, "location: " + mLocation); }
        if (BuildConfig.DEBUG) { Log.d(TAG, "timestamp: " + mTimestamp); }

        ImageView imageView = (ImageView) findViewById(R.id.marker_pic);
        if (mPictureUri != null) {
            imageView.setImageURI(Uri.parse(mPictureUri));
        } else {
            imageView.setImageResource(R.drawable.default_image);
        }
    }

    private void addMarkerToDB(){
        EditText note= (EditText)findViewById(R.id.message_field);
        mNote=note.getText().toString();

        MarkerDBManager.getManager(this).addValue(new MarkerTableEntry(
                mPictureUri,
                mAudioUri,
                mTimestamp,
                mLocation,
                mNote
        ));
    }

    public void saveMarker(View view){
        addMarkerToDB();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void cancelSaveMarker(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }


}
