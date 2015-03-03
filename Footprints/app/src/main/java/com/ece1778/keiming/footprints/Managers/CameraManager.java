package com.ece1778.keiming.footprints.Managers;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.ece1778.keiming.footprints.UI.CameraPreview;
import com.ece1778.keiming.footprints.Utils.FileUtils;

import java.io.File;


public class CameraManager {

    private String TAG = "Camera Handler";
    // Assumes that camera 2 will be added in later.
    private Camera mCamera = null;
    private CameraPreview mPreview;
    private Context mContext;
    private FrameLayout mCameraPreview;
    private CameraInterface cameraInterface;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken");
            File pictureFile = FileUtils.getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            //FileUtils.writeToFile(pictureFile, data);
            //Log.d(TAG, "Path: " + pictureFile.getAbsolutePath().toString());
            //Log.d(TAG, "Uri: " + Uri.fromFile(pictureFile).toString());
            cameraInterface.onPictureTaken(Uri.fromFile(pictureFile));
        }
    };


    public void setCameraInterface (CameraInterface ci) {
        this.cameraInterface = ci;
    }

    public CameraManager(Context context, int id) {
        mContext = context;
        mCameraPreview = (FrameLayout) ((Activity)mContext).findViewById(id);
        mCameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraInterface.onClickListener();
            }
        });
        getCameraAndPreview();
    }

    public void onPause () {
        releaseCameraAndPreview();
    }
    public void onResume() {
        getCameraAndPreview();
    }
    public void takePicture () {
        Log.d (TAG, "takePicture");
        mCamera.takePicture(null, null, mPicture);
    }

    private boolean safeCameraOpen(int id) {
        Log.d (TAG, "sameCameraOpen");
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(TAG, "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        Log.d (TAG, "releaseCameraAndPreview");
        if (mCameraPreview != null) {
            mCameraPreview.removeView(mPreview);
        }
        if (mPreview != null) {
            mPreview.releaseCamera();
        }
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    private void getCameraAndPreview() {
        Log.d (TAG, "getCameraAndPreview");
        // Opens and Sets Camera
        safeCameraOpen(0);
        // Create our Preview view and set it as the content of our activity.
        if (mPreview == null) {
            mPreview = new CameraPreview(mContext, mCamera);
        } else {
            mPreview.setCamera(mCamera);
        }
        mCameraPreview.addView(mPreview);
    }

    // Interface
    public interface CameraInterface {
        void onPictureTaken (Uri uri);
        void onClickListener ();
    }
}
