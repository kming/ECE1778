package com.ece1778.footprints.manager;

/**
 * Created by Don Zhu on 17/03/2015.
 */

import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.R;
import com.ece1778.footprints.database.MarkerDBManager;
import com.ece1778.footprints.database.MarkerTableEntry;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.ece1778.footprints.util.FileUtils.decodeSampledBitmapFromFile;


public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    LayoutInflater inflater = null;
    private TextView textViewTitle;
    private TextView textViewSnippet;
    private ImageView imageViewIcon;
    private static final String TAG = InfoWindowAdapter.class.getName();

    public InfoWindowAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View v = inflater.inflate(R.layout.custom_balloon_layout, null);
        if (marker != null) {
            textViewTitle = (TextView) v.findViewById(R.id.balloon_item_title);
            textViewTitle.setText(marker.getTitle());
            textViewSnippet = (TextView)v.findViewById(R.id.balloon_item_snippet);
            String[] snippet=marker.getSnippet().split(",.,.");
            textViewSnippet.setText(snippet[0]);
            String imageURI=snippet[1];
            String audioURI=snippet[2];

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "getInfoWindow "+ imageURI);
            }

            imageViewIcon = (ImageView)v.findViewById(R.id.balloon_item_image);

            if (imageURI != null && !imageURI.equals("null")) {
                try {
                    ExifInterface exifInterface = new ExifInterface(Uri.parse(imageURI).getPath());
                    if (exifInterface.hasThumbnail()) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Loading Thumbnail");
                        }
                        byte[] data = exifInterface.getThumbnail();
                        imageViewIcon.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                    } else {
                        imageViewIcon.setImageBitmap(
                                decodeSampledBitmapFromFile(new File(Uri.parse(imageURI).getPath()),
                                        300,
                                        300)
                        );
                    }
                } catch (FileNotFoundException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.getMessage());
                    }
                    imageViewIcon.setImageResource(R.drawable.ic_launcher);
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            } else if (!audioURI.equals("null")) {
                //in case where no photo but has audio
                imageViewIcon.setImageResource(R.drawable.default_image_audio_icon);
            }else{
                    // In the case where no photo, set default picture.
                    // TODO: Look into creating a snapshot of the google maps and using that instead.
                    imageViewIcon.setImageResource(R.drawable.ic_launcher);
            }
        }
        return (v);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return (null);
    }
 }
