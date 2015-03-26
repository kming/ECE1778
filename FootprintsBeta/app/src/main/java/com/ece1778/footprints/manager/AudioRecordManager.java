package com.ece1778.footprints.manager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

import static com.ece1778.footprints.util.FileUtils.getOutputMediaFile;

/**
 * Created by Kei-Ming on 2015-03-24.
 */
public class AudioRecordManager {
    private static final String TAG = AudioRecordManager.class.getName();
    private MediaRecorder mAudioRecord = null;
    private static boolean mIsRecording = false;
    private static File recordingFile = null;
    private static AudioRecordManager mManager = null;

    // Recording Parameters
    private static final int SAMPLE_RATE = 44100; // Guaranteed support
    private static final int MAX_LENGTH = 30000; // 30 seconds

    // Media Parameters


    private AudioRecordManager () {
        mAudioRecord = new MediaRecorder();
    }

    private void initRecorder () {
        mAudioRecord.setAudioSource(MediaRecorder.AudioSource.MIC);
        mAudioRecord.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mAudioRecord.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mAudioRecord.setAudioSamplingRate(SAMPLE_RATE);
        recordingFile = getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO);
        mAudioRecord.setOutputFile(recordingFile.getPath());
        mAudioRecord.setAudioChannels(1);
        mAudioRecord.setMaxDuration(MAX_LENGTH);
        try {
            mAudioRecord.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AudioRecordManager getManager () {
        if (mManager == null) {
            mManager = new AudioRecordManager();
        }
        return mManager;
    }

    public Uri start () {
        if (!mIsRecording) {
            mIsRecording = true;
            initRecorder();
            mManager.mAudioRecord.start();
            return Uri.fromFile(recordingFile);
        }
        return null;
    }

    public void stop () throws RuntimeException  {
        if (mIsRecording) {
            mIsRecording = false;
            mManager.mAudioRecord.stop();
            mManager.mAudioRecord.reset();
            recordingFile = null;
        }
    }

    public void reset() {
        mManager.mAudioRecord.reset();
    }

    public static boolean isRecording () {
        return mIsRecording;
    }
}
