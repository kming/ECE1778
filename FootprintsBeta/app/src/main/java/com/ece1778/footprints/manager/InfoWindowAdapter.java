package com.ece1778.footprints.manager;

/**
 * Created by Don Zhu on 17/03/2015.
 */

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ece1778.footprints.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    LayoutInflater inflater = null;
    private TextView textViewTitle;
    private TextView textViewSnippet;
    private ImageView imageViewIcon;

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
            textViewSnippet.setText(marker.getSnippet());
            imageViewIcon = (ImageView)v.findViewById(R.id.balloon_item_image);
            imageViewIcon.setImageResource(R.drawable.default_image);
        }
        return (v);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return (null);
    }
 }
