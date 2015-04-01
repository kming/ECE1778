package com.ece1778.footprints.ui.marker;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.R;
import com.ece1778.footprints.database.LocTableEntry;
import com.ece1778.footprints.database.LocationDBManager;
import com.ece1778.footprints.database.MarkerDBManager;
import com.ece1778.footprints.database.MarkerTableEntry;
import com.ece1778.footprints.ui.MapsActivity;
import com.ece1778.footprints.util.FileUtils;
import com.ece1778.footprints.util.fullscreenUtil.SystemUiHider;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.ece1778.footprints.util.FileUtils.decodeSampledBitmapFromFile;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see com.ece1778.footprints.util.fullscreenUtil.SystemUiHider
 */
public class ViewMarkerActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = 0;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    public static final String IMAGE_KEY = "image";
    public static final String AUDIO_KEY = "audio";
    public static final String TITLE_KEY = "title";
    public static final String NOTE_KEY = "note";
    public static final String TIME_KEY = "time";

    public String savedTime;

    private static final String TAG = ViewMarkerActivity.class.getName();

    private AudioManager am = null;
    MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_marker);
        setupActionBar();

        //final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_view);

        Intent iOrigin = getIntent();
        savedTime=iOrigin.getStringExtra(TIME_KEY);
        Uri mPictureUri = Uri.parse(iOrigin.getStringExtra(IMAGE_KEY));
        String savedTitle=iOrigin.getStringExtra(TITLE_KEY);
        String savedNote=iOrigin.getStringExtra(NOTE_KEY);

        ImageView imageView=(ImageView)findViewById(R.id.landmark_image);
        if (mPictureUri != null) {

            File file = new File(mPictureUri.getPath());

            if (file.exists()) {
                imageView.setImageBitmap(FileUtils.decodeFile(file, 500));
            } else {
                imageView.setImageResource(R.drawable.default_image);
            }
        } else {
            // In the case where no photo, set default picture.
            // TODO: Look into creating a snapshot of the google maps and using that instead.
            imageView.setImageResource(R.drawable.default_image);

        }

        TextView title = (TextView) findViewById(R.id.landmark_title);
        if (!savedTitle.isEmpty()) {
            title.setVisibility(View.VISIBLE);
            title.setText(savedTitle);
        }else{
            title.setVisibility(View.INVISIBLE);
        }

        TextView note = (TextView) findViewById(R.id.landmark_description);
        if (!savedNote.isEmpty()) {
            note.setVisibility(View.VISIBLE);
            note.setText(savedNote);
        }else{
            note.setVisibility(View.INVISIBLE);
        }

        String savedAudio=iOrigin.getStringExtra(AUDIO_KEY);
        ToggleButton audioPlaybackState=(ToggleButton)findViewById(R.id.toggleSoundButton);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "savedAudio:"+ savedAudio);
        }
        if (!savedAudio.equals("null")) {
            audioPlaybackState.setVisibility(View.VISIBLE);
            audioPlaybackState.setChecked(false);
            mediaPlayer = MediaPlayer.create(this, Uri.parse(savedAudio));
        }else{
            audioPlaybackState.setVisibility(View.INVISIBLE);
        }

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);



        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
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
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer!=null) {
            stopMusic();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
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


    public void deleteMarker(View v){

        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setMessage("Delete this landmark permanently?").setCancelable(true);
        adBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ArrayList<MarkerTableEntry> entries = MarkerDBManager.getManager(ViewMarkerActivity.this).getAllValues();
                String time=new String();

                // From the locations, determine which grid it belongs in.
                for (MarkerTableEntry entry : entries) {
                    time = entry.getTime();
                    if (time.equals(savedTime)){
                        MarkerDBManager.getManager(ViewMarkerActivity.this).deleteValue(entry);
                        Toast.makeText(ViewMarkerActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                Intent i = new Intent(ViewMarkerActivity.this, MapsActivity.class);
                startActivity(i);
                dialog.dismiss();
            }
        });
        adBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = adBuilder.create();
        alertDialog.show();

        return;
    }

    public void playAudio(View v){
        boolean on = ((ToggleButton) v).isChecked();

        if (on) {
            playMusic();
        } else {
            mediaPlayer.pause();
        }
        return;
    }

    private void playMusic () {
        int result = am.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        final AlertDialog.Builder alertDialogBuilder;
        if (am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {

            alertDialogBuilder = new AlertDialog.Builder(this);
            // set dialog message
            alertDialogBuilder.setMessage("Turn up volume for full experience").setCancelable(true);
            alertDialogBuilder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            dialog.dismiss();
                        }
                    });
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.show();
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            startPlayback();
        }
    }

    private void stopMusic () {
        // Abandon audio focus when playback complete
        am.abandonAudioFocus(afChangeListener);
        stopPlayback();
    }

    private void startPlayback () {

        mediaPlayer.setLooping(true);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    private void stopPlayback () {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
                stopPlayback();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                startPlayback();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                am.abandonAudioFocus(afChangeListener);
                stopPlayback();
            }
        }
    };
}
