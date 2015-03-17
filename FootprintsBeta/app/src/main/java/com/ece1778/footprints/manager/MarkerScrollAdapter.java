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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.ece1778.footprints.util.FileUtils.decodeSampledBitmapFromFile;

public class MarkerScrollAdapter extends RecyclerView.Adapter<MarkerScrollAdapter.MarkerScrollViewHolder> {

    private static final String TAG = MarkerScrollAdapter.class.getName();
    private List<MarkerTableEntry> markerList;
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

        String mPictureUri = marker.getPicture();
        if (mPictureUri != null) {
            try {
                ExifInterface exifInterface = new ExifInterface(Uri.parse(mPictureUri).getPath());
                if (exifInterface.hasThumbnail()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Loading Thumbnail"); }
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
                if (BuildConfig.DEBUG) {Log.e (TAG, e.getMessage());}
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {Log.e (TAG, e.getMessage());}
            }
        } else {
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

    public static class MarkerScrollViewHolder extends RecyclerView.ViewHolder {

        protected TextView vNote;
        protected TextView vTitle;
        protected ImageView vImage;

        public MarkerScrollViewHolder(View v) {
            super(v);
            vNote = (TextView)  v.findViewById(R.id.cardStory);
            vTitle = (TextView) v.findViewById(R.id.cardTitle);
            vImage = (ImageView) v.findViewById(R.id.cardImage);
        }
    }
}