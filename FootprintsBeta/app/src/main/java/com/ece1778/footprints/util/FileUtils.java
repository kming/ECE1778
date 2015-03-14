package com.ece1778.footprints.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;


import com.ece1778.footprints.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kei-Ming on 2015-02-24.
 */
public class FileUtils {
    public static final String TAG = FileUtils.class.getName(); // Debug Tag

    private FileUtils() { /* empty constructor */ } // Used as a package of utility functions

    // Copy File Utility - Uses a string as input instead of filestreams
    public static void copyFile(String fromFileString, String toFileString) throws IOException {
        File fromFile = new File(fromFileString);
        File toFile = new File(toFileString);
        // Converts paths to file streams and calls internal function
        copyFile(new FileInputStream(fromFile), new FileOutputStream(toFile));
    }

    // Copy File Utility using File streams - allows copying from internal to external locations
    // Allows users to use this as sometimes it takes much more effort to determine the path without
    // the file anyways.
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        fromChannel = fromFile.getChannel();
        toChannel = toFile.getChannel();
        fromChannel.transferTo(0, fromChannel.size(), toChannel);

        // clean up after copy
        if (fromChannel != null) {
            fromChannel.close();
        }
        if (toChannel != null) {
            toChannel.close();
        }

    }

    // Move file function.  Less robust since it will not handle movement from different file systems
    public static void moveFile(String fromFileString, String toFileString) throws IOException {
        File fromFile = new File(fromFileString);
        File toFile = new File(toFileString);
        fromFile.renameTo(toFile);
    }

    // More robust since it will handle different filesystems.
    public static void moveFileRobust(String fromFileString, String toFileString) throws IOException {
        // Copies the file (regardless if it is on different file systems
        copyFile(fromFileString, toFileString);
        // delete
        new File(fromFileString).delete();
    }

    // gets the list of files in the directory listed.
    public static String[] getFiles(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            return dir.list();
        }
        return null;
    }

    // Write data to file.
    public static void writeToFile(File file, byte[] data) throws IOException, FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

    // Create a file Uri for saving an image or video *
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    // Create a File for saving an image or video
    public static File getOutputMediaFile(int type) {
        // To be safe, check to make sure external storage is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Const.MEDIA_DIRECTORY), "FootPrints");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "failed to create directory");
                }
                return null;
            }
        }

        // Create a media file with the time stamp in the filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == FileColumns.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == FileColumns.MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    //decodes image and scales it to reduce memory consumption
    public static Bitmap decodeFile(File f, final int size) {
        // size refers to the final size, approximately 100 for a preview is adequate,
        // scales to ~100 pixels (exact scaled size depends on aspect ratio)
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);


            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= size && o.outHeight / scale / 2 >= size)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight)
            throws FileNotFoundException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(new FileInputStream(file), null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
    }
}
