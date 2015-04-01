package com.ece1778.footprints.manager;

/**
 * Created by Don Zhu on 16/03/2015.
 */
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.R;
import com.ece1778.footprints.database.MarkerTableEntry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.ece1778.footprints.util.FileUtils.decodeSampledBitmapFromFile;

public class MarkerScrollAdapter extends RecyclerView.Adapter<MarkerScrollAdapter.MarkerScrollViewHolder> {

    private static final String TAG = MarkerScrollAdapter.class.getName();
    private List<MarkerTableEntry> markerList;
    private onClickedListener mListener = null;

    public MarkerScrollAdapter(List<MarkerTableEntry> markerList) {
        this.markerList = markerList;
    }

    @Override
    public int getItemCount() {
        return markerList.size();
    }

    @Override
    public void onBindViewHolder(MarkerScrollViewHolder markerScrollViewHolder, int i) {
        MarkerTableEntry marker = markerList.get(i);
        markerScrollViewHolder.vNote.setText(marker.getNote());
        markerScrollViewHolder.vTitle.setText(marker.getTitle());

        String loc=marker.getLocation();
        String[] latlong=  loc.split(",");
        double latitude = Double.parseDouble(latlong[0])-.012;
        double longitude = Double.parseDouble(latlong[1])+.001;
        markerScrollViewHolder.vLocation=new LatLng(latitude,longitude);

        String mPictureUri = marker.getPicture();
        String mAudioUri=marker.getAudio();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Loading Audio File "+mAudioUri);
        }
        if (mPictureUri != null) {
            try {
                ExifInterface exifInterface = new ExifInterface(Uri.parse(mPictureUri).getPath());
                if (exifInterface.hasThumbnail()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Loading Thumbnail");
                    }
                    byte[] data = exifInterface.getThumbnail();
                    markerScrollViewHolder.vImage.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                } else {
                    markerScrollViewHolder.vImage.setImageBitmap(
                            decodeSampledBitmapFromFile(new File(Uri.parse(mPictureUri).getPath()),
                                    300,
                                    300)
                    );
                }
            } catch (FileNotFoundException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.getMessage());
                }
                markerScrollViewHolder.vImage.setImageResource(R.drawable.default_image);
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }else if (mAudioUri!=null){
            // In the case where has audio but no photo, set default picture.
            markerScrollViewHolder.vImage.setImageResource(R.drawable.default_image_audio);
        }else {
            // In the case where no photo, set default picture.
            // TODO: Look into creating a snapshot of the google maps and using that instead.
            markerScrollViewHolder.vImage.setImageResource(R.drawable.default_image);
        }
    }

    @Override
    public MarkerScrollViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new MarkerScrollViewHolder(itemView);
    }

    public class MarkerScrollViewHolder extends RecyclerView.ViewHolder {

        protected TextView vNote;
        protected TextView vTitle;
        protected LatLng vLocation;
        protected ImageView vImage;

        public MarkerScrollViewHolder(View v) {
            super(v);
            vNote = (TextView) v.findViewById(R.id.cardStory);
            vTitle = (TextView) v.findViewById(R.id.cardTitle);
            vImage = (ImageView) v.findViewById(R.id.cardImage);


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onChanged(vLocation);
                    } else {
                        if (BuildConfig.DEBUG) { Log.d(TAG, "Listener Not Set"); }
                    }
                }
            });
        }
    }

    public void setOnClickedListener (onClickedListener listener) {
        mListener = listener;
    }

    public interface onClickedListener {
        void onChanged(LatLng coordinates);
    }

}