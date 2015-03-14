package com.ece1778.footprints.ui.marker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.R;
import com.ece1778.footprints.database.MarkerDBManager;
import com.ece1778.footprints.database.MarkerTableEntry;
import com.ece1778.footprints.ui.MapsActivity;
import com.ece1778.footprints.util.fullscreenUtil.SystemUiHider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.ece1778.footprints.util.FileUtils.decodeSampledBitmapFromFile;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class AddMarkerActivity extends Activity {
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
    //private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private static final int HIDER_FLAGS = 0;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    /**
     * Class Variables.
     */
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
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_marker);
        setupActionBar();

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

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.).setOnTouchListener(mDelayHideTouchListener);

        // Populate Elements after the view is created, so the size is known.
        populateElements();
    }

    @Override
    public View onCreateView (View parent, String name, Context context, AttributeSet attrs) {
        View root = super.onCreateView(parent, name, context, attrs);


        return root;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // to prevent out of memory error, on destroy of activity, destroy the bitmap used.
        ImageView imageView = (ImageView) findViewById(R.id.marker_pic);
        BitmapDrawable bd = (BitmapDrawable) imageView.getDrawable();
        if (bd != null) {
            bd.getBitmap().recycle();
            imageView.setImageBitmap(null);
        }

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
            try {
                ExifInterface exifInterface = new ExifInterface(Uri.parse(mPictureUri).getPath());
                if (exifInterface.hasThumbnail()) {
                    if (BuildConfig.DEBUG) {Log.d (TAG, "Loading Thumbnail"); }
                    byte[] data = exifInterface.getThumbnail();
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                } else {
                    imageView.setImageBitmap(
                            decodeSampledBitmapFromFile(new File(Uri.parse(mPictureUri).getPath()),
                                    300,
                                    300)
                    );
                }
            } catch (FileNotFoundException e) {
                if (BuildConfig.DEBUG) {Log.e (TAG, e.getMessage());}
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {Log.e (TAG, e.getMessage());}
            }
        } else {
            // In the case where no photo, set default picture.
            // TODO: Look into creating a snapshot of the google maps and using that instead.
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

    public void cancelMarker(View view){
        // Delete any of the files that were created in the process.
        if (mPictureUri != null && !mPictureUri.isEmpty()) {
            new File(Uri.parse(mPictureUri).getPath()).delete();
        }
        if (mAudioUri != null &&!mAudioUri.isEmpty()) {
            new File(Uri.parse(mAudioUri).getPath()).delete();
        }
        // Finish with adding
        finish();
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
}
