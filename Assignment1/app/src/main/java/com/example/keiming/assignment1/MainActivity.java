package com.example.keiming.assignment1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private String mainFragTag = "mainFragmentTag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainFragment mainFrag = new mainFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainFrag, mainFragTag) // adds a tag
                    .commit();
        }
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
        if (id == R.id.action_reset) {
            mainFragment mainFrag = (mainFragment) this.getSupportFragmentManager().findFragmentByTag(mainFragTag);
            mainFrag.reset();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        OrientationUtils.unlockOrientation(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        OrientationUtils.setOrientationPortrait(this);
    }

    /**
     * A main fragment containing a simple view.
     */
    public static class mainFragment extends Fragment {
        private String textBoxTextDefault = "No Clicks Yet";
        private TextView textBox;
        private ImageView imageBox;
        private boolean imageDefaultState;
        private Integer numClicks;
        private AudioManager am = null;
        MediaPlayer mediaPlayer = null;
        public mainFragment() {
        }

        @Override
        public void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // setup music
            this.getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            am = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            // Initialize textBox and imageBox with default values.
            textBox = (TextView) rootView.findViewById(R.id.textBox);
            textBox.setText(textBoxTextDefault);
            numClicks = 0;
            imageBox = (ImageView) rootView.findViewById(R.id.imageBox);
            imageBox.setImageResource(android.R.color.transparent);
            imageDefaultState = true;

            // Initialize Button on Click Listeners. Can be done with XML,
            // but better to populate listeners when necessary
            Button clickButton = (Button) rootView.findViewById(R.id.clickButton);
            Button imageButton = (Button) rootView.findViewById(R.id.imageButton);

            clickButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    clickButtonClicked(v);
                }
            });
            imageButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    imageButtonClicked(v);
                }
            });

            return rootView;
        }

        @Override
        public void onStop() {
            super.onStop();
            stopMusic();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mediaPlayer.release();
        }

        // Register button click, increment counter.  This will register even when
        // image is toggled.
        public void clickButtonClicked(View v) {
            this.numClicks++;
            textBox.setText("Clicked " + numClicks + " times.");
        }

        // Toggles Image
        public void imageButtonClicked(View v) {
            this.imageDefaultState = !this.imageDefaultState;
            if (this.imageDefaultState) {
                imageBox.setImageResource(android.R.color.transparent);
                textBox.setVisibility(View.VISIBLE);
                stopMusic();
            } else {
                imageBox.setImageResource(R.drawable.awesome);
                textBox.setVisibility(View.INVISIBLE);
                playMusic();
            }
        }

        private void reset () {
            resetImageBoxDefault();
            resetTextBoxDefault();
        }
        private void resetImageBoxDefault() {
            this.imageDefaultState = true;
            imageBox.setImageResource(android.R.color.transparent);
            stopMusic();
        }
        private void resetTextBoxDefault(){
            this.numClicks = 0;
            textBox.setText(textBoxTextDefault);
            textBox.setVisibility(View.VISIBLE);
        }

        // Probably shouldn't play music on the UI thread, but too lazy to make it work
        // asynchronously and on worker threads atm.
        private void playMusic () {
            int result = am.requestAudioFocus(afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);
            final AlertDialog.Builder alertDialogBuilder;
            if (am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {

                alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
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
            mediaPlayer = MediaPlayer.create(this.getActivity(), R.raw.awesome);
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
}
