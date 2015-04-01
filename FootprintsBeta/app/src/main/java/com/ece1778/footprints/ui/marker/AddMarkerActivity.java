package com.ece1778.footprints.ui.marker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    private String mTitle = null;

    private static int RESULT_LOAD_IMAGE = 1;

    private AudioManager am = null;
    MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_marker);
        setupActionBar();

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        Intent i = getIntent();
        mPictureUri = i.getStringExtra(PIC_URI_KEY);
        mAudioUri   = i.getStringExtra(AUDIO_URI_KEY);
        mLocation = i.getStringExtra(LOCATION_KEY);
        mTimestamp = i.getStringExtra(TIMESTAMP_KEY);

        if(mPictureUri!=null){
            CheckBox sight=(CheckBox)findViewById(R.id.checkbox_sight);
            sight.setChecked(true);
        }

        if(mAudioUri!=null){
            CheckBox sound=(CheckBox)findViewById(R.id.checkbox_sound);
            sound.setChecked(true);
        }
        populateElements();

        ToggleButton audioPlaybackState=(ToggleButton)findViewById(R.id.toggleSoundButtonPreview);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "mAudioUri:"+ mAudioUri);
        }
        if (mAudioUri!=null) {
            audioPlaybackState.setVisibility(View.VISIBLE);
            audioPlaybackState.setChecked(false);
            mediaPlayer = MediaPlayer.create(this, Uri.parse(mAudioUri));
        }else{
            audioPlaybackState.setVisibility(View.INVISIBLE);
        }

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
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
            imageView.setImageBitmap(null);
        }

    }

    private void populateElements () {
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
            imageView.setImageResource(R.drawable.default_image2);


            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent gallery = new Intent(
                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(gallery, RESULT_LOAD_IMAGE);
                    CheckBox sight=(CheckBox)findViewById(R.id.checkbox_sight);
                    sight.setChecked(true);
                }

            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.marker_pic);
            mPictureUri=picturePath;
            try {
                ExifInterface exifInterface = new ExifInterface(Uri.parse(mPictureUri).getPath());
                if (exifInterface.hasThumbnail()) {
                    if (BuildConfig.DEBUG) {Log.d (TAG, "Loading Thumbnail"); }
                    byte[] data2 = exifInterface.getThumbnail();
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(data2, 0, data2.length));
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


        }


    }

    private void addMarkerToDB(){
        EditText note= (EditText)findViewById(R.id.message_field);
        mNote=note.getText().toString();
        String tempNote=mNote;

        CheckBox sight=(CheckBox)findViewById(R.id.checkbox_sight);
        CheckBox sound=(CheckBox)findViewById(R.id.checkbox_sound);
        CheckBox scent=(CheckBox)findViewById(R.id.checkbox_scent);
        CheckBox story=(CheckBox)findViewById(R.id.checkbox_text);
        CheckBox other=(CheckBox)findViewById(R.id.checkbox_other);

        if (sight.isChecked()){
            mNote+="====1";
        }

        if (sound.isChecked()){
            mNote+="====2";
        }

        if (tempNote!=null &&story.isChecked()){
            mNote+="====3";
        }

        if (tempNote!=null && scent.isChecked()){
            mNote+="====4";
        }

        if (other.isChecked()||(!sight.isChecked()&&!scent.isChecked()&&!story.isChecked()&&!scent.isChecked())){
            mNote+="====5";
        }

        EditText title= (EditText)findViewById(R.id.location_field);
        mTitle=title.getText().toString();

        MarkerDBManager.getManager(this).addValue(new MarkerTableEntry(
                mPictureUri,
                mAudioUri,
                mTimestamp,
                mLocation,
                mNote,
                mTitle
        ));

        if (BuildConfig.DEBUG) { Log.d(TAG, "Added picture: " + mPictureUri); }
        if (BuildConfig.DEBUG) { Log.d(TAG, "Added audio: " + mAudioUri); }
        if (BuildConfig.DEBUG) { Log.d(TAG, "Added location: " + mLocation); }
        if (BuildConfig.DEBUG) { Log.d(TAG, "Added note: " + mNote); }
        if (BuildConfig.DEBUG) { Log.d(TAG, "Added title: " + mTitle); }
        if (BuildConfig.DEBUG) { Log.d(TAG, "Added timestamp: " + mTimestamp); }
    }

    public void saveMarker(View view){
        EditText title=(EditText)findViewById(R.id.location_field);
        if (!title.getText().toString().isEmpty()) {
            addMarkerToDB();
            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(this, "Please name the spot.", Toast.LENGTH_SHORT).show();
        }
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
    protected void onStop() {
        super.onStop();
        if(mediaPlayer!=null) {
            stopMusic();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
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

    public void playAudioPreview(View v){
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
