package com.ece1778.keiming.footprints.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.ece1778.keiming.footprints.BuildConfig;
import com.ece1778.keiming.footprints.R;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = CameraPreview.class.getName();
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context mContext;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mContext = context;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d (TAG, "surfaceChanged");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(100);
        parameters.setPictureSize(sizes.get(0).width, sizes.get(0).height); // largest size
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        // parameter changes the picture EXIF orientation
        // Display Orientation changes the preview shown so that it is always correct for viewer
        if(display.getRotation() == Surface.ROTATION_0){
            parameters.set("rotation",90);
            mCamera.setDisplayOrientation(90);
        }else if(display.getRotation() == Surface.ROTATION_90){//1
            parameters.set("rotation",0);
            mCamera.setDisplayOrientation(0);
        }else if(display.getRotation() == Surface.ROTATION_180){//2
            parameters.set("rotation",270);
            mCamera.setDisplayOrientation(270);
        }else if(display.getRotation() == Surface.ROTATION_270){//3
            parameters.set("rotation",180);
            mCamera.setDisplayOrientation(180);
        }
        // start preview with new settings
        try {
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            if (BuildConfig.DEBUG) {Log.d(TAG, "Error starting camera preview: " + e.getMessage());}
        }
    }

    public void releaseCamera() {
        mCamera = null;
    }

    public void setCamera (Camera camera) {
        mCamera = camera;
    }
}