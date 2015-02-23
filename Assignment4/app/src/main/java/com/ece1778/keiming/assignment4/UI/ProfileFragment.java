package com.ece1778.keiming.assignment4.UI;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ece1778.keiming.assignment4.BuildConfig;
import com.ece1778.keiming.assignment4.InternalClasses.TableEntry;
import com.ece1778.keiming.assignment4.Managers.DatabaseManager;
import com.ece1778.keiming.assignment4.R;

import java.io.InputStream;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PROFILE_ID = "profileNum";

    // TODO: Rename and change types of parameters
    private int mID;
    private int mCount = 0;


    public static ProfileFragment newInstance(int param1, int count) {
        ProfileFragment pF = newInstance(param1);
        pF.mCount = count;
        return pF;
    }

    public static ProfileFragment newInstance(int param1) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROFILE_ID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mID = getArguments().getInt(ARG_PROFILE_ID);
        }
    }
    @Override
    public void onResume () {
        super.onResume();
     /*
     // Really slow and doesn't show right number
        if (mCount != 0) {
            Toast.makeText(
                    this.getActivity(),
                    Integer.toString(mID-1)+"/"+Integer.toString(mCount),
                    Toast.LENGTH_SHORT
            ).show();
        }
    */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        TableEntry entry = DatabaseManager.getManager(this.getActivity()).getValue(mID);

        ((TextView) root.findViewById(R.id.profile_name)).setText(entry.getName());
        ((TextView) root.findViewById(R.id.profile_bio)).setText(entry.getNote());
        new DownloadImageTask(
                ((ImageView) root.findViewById(R.id.profile_pic))
        ).execute(entry.getPath());

        return root;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { Log.e("Error", e.getMessage()); }
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
