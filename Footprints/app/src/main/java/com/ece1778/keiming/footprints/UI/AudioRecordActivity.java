package com.ece1778.keiming.footprints.UI;

        import android.app.Activity;
        import android.content.Intent;
        import android.support.v7.app.ActionBarActivity;
        import android.widget.LinearLayout;
        import android.os.Bundle;
        import android.os.Environment;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.content.Context;
        import android.util.Log;
        import android.media.MediaRecorder;
        import android.media.MediaPlayer;

        import com.ece1778.keiming.footprints.R;

        import java.io.IOException;


public class AudioRecordActivity extends ActionBarActivity
{
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    private boolean mStartPlaying = false;
    private boolean mStartRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;


    }

    public void startStopRecording(View view){

        Button btn=(Button) findViewById(R.id.playback_btn);
        onRecord(mStartRecording);

        if (mStartRecording) {
            btn.setText("Stop recording");

        } else {
            btn.setText("Start recording");
        }
        mStartRecording = !mStartRecording;
    }

    public void playBackButton(View view){

        Button btn=(Button) findViewById(R.id.start_record_btn);
        onPlay(mStartPlaying);
        if (mStartPlaying) {
            btn.setText("Stop playing");
        } else {
            btn.setText("Start playing");
        }
        mStartPlaying = !mStartPlaying;

    }


    public AudioRecordActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }



    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void cancelSaveAudio(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void continueSavingAudio(View view){
        Intent i = new Intent(this, AddMarkerActivity.class);
        //i.putExtra(AddMarkerActivity.AUDIO_URI_KEY, uri.toString());
        startActivity(i);
    }
}