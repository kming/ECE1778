package com.ece1778.keiming.footprints.UI;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ece1778.keiming.footprints.Classes.LocTableEntry;
import com.ece1778.keiming.footprints.Classes.MarkerTableEntry;
import com.ece1778.keiming.footprints.Managers.LocationDBManager;
import com.ece1778.keiming.footprints.Managers.LocationManager;
import com.ece1778.keiming.footprints.Managers.MarkerDBManager;
import com.ece1778.keiming.footprints.R;
import com.ece1778.keiming.footprints.Utils.GeneralUtils;

import java.net.URI;

public class AddMarkerActivity extends ActionBarActivity {
    public static final String PIC_URI_KEY = "pictureUri";
    public static final String AUDIO_URI_KEY = "audioUri";

    private String mPictureUri = null;
    private String mAudioUri = null;
    private String mNote= null;

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

        if (mPictureUri != null) {
            ImageView imageView = (ImageView) findViewById(R.id.marker_pic);
            imageView.setImageURI(Uri.parse(mPictureUri));
        }


    }

    private void populateMarkerDB(){

        EditText note= (EditText)findViewById(R.id.message_field);
        mNote=note.getText().toString();

        Location location= LocationManager.getManager(this).getLocation();
        String timeString = GeneralUtils.timeMilliToString(location.getTime());

        MarkerDBManager.getManager(this).addValue(new MarkerTableEntry(
                mPictureUri,
                mAudioUri,
                GeneralUtils.locationToString(location),
                mNote,
                timeString
        ));

    }

    private void saveMarker(View view){
        populateMarkerDB();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void cancelSaveMarker(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }


}
