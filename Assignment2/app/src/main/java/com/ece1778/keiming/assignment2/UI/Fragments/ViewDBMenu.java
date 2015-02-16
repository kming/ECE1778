package com.ece1778.keiming.assignment2.UI.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ece1778.keiming.assignment2.Database.DatabaseHandler;


import java.io.File;
import java.io.IOException;


public class ViewDBMenu extends ListFragment {

    private ArrayAdapter<String> adapter = null;
    // TODO: Rename and change types of parameters
    public static ViewDBMenu newInstance() {
        ViewDBMenu fragment = new ViewDBMenu();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ViewDBMenu() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the list of databases in the DB folder;
        File dir = new File(DatabaseHandler.getDBPathDir());
        if (!dir.exists()) {
            dir.mkdir();
        }
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1,
                    dir.list());
        setListAdapter(adapter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public void onResume () {
        super.onResume();
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        try {
            TextView textView = (TextView) v;
            DatabaseHandler.getHandler().importDatabase(textView.getText().toString());
            // Go back to previous fragment
            this.getActivity().getSupportFragmentManager().popBackStackImmediate();
        } catch (IOException e) {
            throw new ClassCastException(getActivity().toString()
                    + " IO Exception " + e.toString());
        }
    }

}
