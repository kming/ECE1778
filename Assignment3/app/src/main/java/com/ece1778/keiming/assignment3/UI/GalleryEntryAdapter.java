package com.ece1778.keiming.assignment3.UI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ece1778.keiming.assignment3.BackendHandlers.Database.DatabaseHandler;
import com.ece1778.keiming.assignment3.BackendHandlers.Database.TableEntry;
import com.ece1778.keiming.assignment3.R;
import com.ece1778.keiming.assignment3.Utils.FileUtils;

/**
 * Created by Kei-Ming on 2015-02-06.
 */
public class GalleryEntryAdapter extends RecyclerView.Adapter<GalleryEntryAdapter.ViewHolder> {
    private static GalleryEntryAdapter galleryHandler = null;
    private ArrayList<TableEntry> mDataset;


    private Context mContext;
    private final Uri defaultImageUri = Uri.parse(
                                        "android.resource://com.ece1778.keiming.assignment3/" +
                                        R.drawable.ic_image_default);
    private final String defaultImagePath = defaultImageUri.getPath();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imgPicture;
        public TextView txtTitle;
        public TextView txtPosition;

        public ViewHolder(View v) {
            super(v);
            imgPicture  = (ImageView)   v.findViewById(R.id.entryImage);
            txtTitle    = (TextView)    v.findViewById(R.id.entryTitle);
            txtPosition = (TextView)    v.findViewById(R.id.entryPosition);
        }
    }

    public void add(TableEntry item) {
        int position = mDataset.size();
        mDataset.add(position, item);
        DatabaseHandler.getHandler().addValue(item);
        notifyItemInserted(position);
    }

    public void remove(TableEntry item) {
        int position = mDataset.indexOf(item);
        if ((position >= 0) && (getItemCount() > position)) {
            File file = new File(Uri.parse(mDataset.get(position).getPath()).getPath());
            file.delete();
            mDataset.remove(position);
            DatabaseHandler.getHandler().deleteValue(item);
            notifyItemRemoved(position);
        }
    }

    // Provide a private constructor
    private GalleryEntryAdapter() {
        return;
    }


    public static GalleryEntryAdapter initHandler (Context context) {
        if (galleryHandler == null) {
            galleryHandler = new GalleryEntryAdapter();
            galleryHandler.mContext = context;
            galleryHandler.mDataset = DatabaseHandler.getHandler().getAllValues();
        }
        return galleryHandler;
    }

    public static GalleryEntryAdapter getGalleryHandler () {
        return galleryHandler;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GalleryEntryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_entry_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final TableEntry entry = mDataset.get(position);
        final Uri uri = Uri.parse(mDataset.get(position).getPath());
        File file = new File(uri.getPath());
        if (file.exists()) {
            holder.imgPicture.setImageBitmap(FileUtils.decodeFile(file, 100));
        } else {
            holder.imgPicture.setImageURI(defaultImageUri);
        }
        holder.imgPicture.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                remove(entry);
                return false;
            }
        });
        holder.imgPicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(uri, "image/jpeg");
                mContext.startActivity(i);
                return;
            }
        });

        holder.txtTitle.setText(mDataset.get(position).getNote());
        holder.txtPosition.setText(mDataset.get(position).getLocation());

    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
