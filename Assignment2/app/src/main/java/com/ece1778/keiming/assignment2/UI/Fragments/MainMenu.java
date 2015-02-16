package com.ece1778.keiming.assignment2.UI.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ece1778.keiming.assignment2.Database.DatabaseHandler;
import com.ece1778.keiming.assignment2.R;
import com.ece1778.keiming.assignment2.UI.ViewActivity;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainMenu.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMenu#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMenu extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MENU_1 = "menu1";
    private static final String ARG_MENU_2 = "menu2";

    // TODO: Rename and change types of parameters
    private String mMenu1;
    private String mMenu2;


    private static EditText filenameView;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param menu1 Parameter 1.
     * @param menu2 Parameter 2.
     * @return A new instance of fragment MainMenu.
     */
    public static MainMenu newInstance(String menu1, String menu2) {
        MainMenu fragment = new MainMenu();
        Bundle args = new Bundle();
        args.putString(ARG_MENU_1, menu1);
        args.putString(ARG_MENU_2, menu2);
        fragment.setArguments(args);
        return fragment;
    }

    public MainMenu() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filenameView = new EditText(getActivity());
        if (getArguments() != null) {
            // Want to get arguments only when the fragment is created.
            mMenu1 = getArguments().getString(ARG_MENU_1);
            mMenu2 = getArguments().getString(ARG_MENU_2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);

        // Use the central function which calls the correct functionality based on the view ID
        Button bt = (Button) rootView.findViewById(R.id.mmAddButton);
        bt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onButtonPressed(view);
                    }
                }
        );
        bt = (Button) rootView.findViewById(R.id.mmViewButton);
        bt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onButtonPressed(view);
                    }
                }
        );
        bt = (Button) rootView.findViewById(R.id.mmStoreButton);
        bt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onButtonPressed(view);
                    }
                }
        );
        bt = (Button) rootView.findViewById(R.id.mmLoadButton);
        bt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onButtonPressed(view);
                    }
                }
        );
        bt = (Button) rootView.findViewById(R.id.mmExitButton);
        bt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onButtonPressed(view);
                    }
                }
        );
        return rootView;
    }

    // Determines which view caused the button press and react based on it.
    public void onButtonPressed(View view ) {
        if (mListener != null) {
            mListener.onFragmentInteraction(view);
        }
        switch (view.getId()) {
            case R.id.mmAddButton:
                // Switch to Add Fragment
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment newFrag = AddMenu.newInstance();
                ft.replace(R.id.MainContainer, newFrag);
                ft.addToBackStack("Switch to Add Fragment");
                ft.commit();
                break;
            case R.id.mmViewButton:
                // Switch to View Activity
                Intent intent = new Intent(this.getActivity(), ViewActivity.class);
                startActivity(intent);
                break;
            case R.id.mmStoreButton:
                // Store the current database

                AlertDialog.Builder adBuilder = new AlertDialog.Builder(this.getActivity());
                adBuilder.setMessage("Enter Database Name").setCancelable(true);
                adBuilder.setView(filenameView);
                adBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            DatabaseHandler.getHandler().exportDatabase(filenameView.getText().toString());
                        } catch (IOException e) {
                            throw new ClassCastException(getActivity().toString()
                                    + " IO Exception " + e.toString());
                        }
                    }
                });
                adBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = adBuilder.create();
                alertDialog.show();
                break;
            case R.id.mmLoadButton:
                // Switch to Load Fragment
                fm = getActivity().getSupportFragmentManager();
                ft = fm.beginTransaction();
                newFrag = ViewDBMenu.newInstance();
                ft.replace(R.id.MainContainer, newFrag);
                ft.addToBackStack("Switch to Add Fragment");
                ft.commit();
                break;
            case R.id.mmExitButton:
                // Exit Procedure
                this.getActivity().finish();
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onAddClicked(View v) {
        // Add Entry Fragment Switch
    }

    public void onViewClicked(View v) {
        // Use Intents and switch to the new activity: ViewListActivity
    }

    public void onStoreClicked(View v) {
        // Store database call. --> Use a separate thread instead of the UI thread.
        // Can do a popup dialog instead of fragment switching
    }

    public void onLoadClicked(View v) {
        // Switch to Load Fragment --> Choose which database to load
    }

    public void onExitClicked(View v) {
        // Save the entries to the file and then exit program
    }

    // Allows a public override of the button listener
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(View view);
    }

}
