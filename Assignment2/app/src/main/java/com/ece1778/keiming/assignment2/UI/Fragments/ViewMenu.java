package com.ece1778.keiming.assignment2.UI.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ece1778.keiming.assignment2.Database.DatabaseHandler;
import com.ece1778.keiming.assignment2.R;
import com.ece1778.keiming.assignment2.classes.DataEntry;
import com.ece1778.keiming.assignment2.classes.EntryAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * interface.
 */
public class ViewMenu extends ListFragment  {

    EntryAdapter entryAdapter;
    public static ViewMenu newInstance() {
        ViewMenu fragment = new ViewMenu();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ViewMenu() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the list of entries from the database
        DatabaseHandler handler = DatabaseHandler.getHandler();
        entryAdapter = new EntryAdapter (getActivity(), handler.getAllValues());
        setListAdapter(entryAdapter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume () {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }


    public void addEntry(String name, int age, String food) {
        entryAdapter.add(new DataEntry(name, age, food));
        entryAdapter.notifyDataSetChanged();
    }
}
