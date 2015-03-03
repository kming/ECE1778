package com.ece1778.keiming.footprints.UI;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ece1778.keiming.footprints.R;

import java.net.URI;

public class AddMarkerActivity extends ActionBarActivity {
    public static final String PIC_URI_KEY = "pictureUri";
    public static final String AUDIO_URI_KEY = "audioUri";


    private String mPictureUri = null;
    private String mAudioUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);
        populateElements();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_marker, menu);
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
        }

        return super.onOptionsItemSelected(item);
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
}
