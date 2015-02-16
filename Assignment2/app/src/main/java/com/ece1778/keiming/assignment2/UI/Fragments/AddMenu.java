package com.ece1778.keiming.assignment2.UI.Fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.ece1778.keiming.assignment2.classes.DataEntry;
import com.ece1778.keiming.assignment2.Database.DatabaseHandler;
import com.ece1778.keiming.assignment2.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddMenu#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddMenu extends Fragment {
    private View rootView;
    private OnAddListener onAdd;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddMenu.
     */
    public static AddMenu newInstance() {
        AddMenu fragment = new AddMenu();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AddMenu() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_add_menu, container, false);
        Button bt = (Button) rootView.findViewById(R.id.amAddButton);
        bt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onAddPressed(view);
                    }
                }
        );
        bt = (Button) rootView.findViewById(R.id.amCancelButton);
        bt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onCancelPressed(view);
                    }
                }
        );
        bt = (Button) rootView.findViewById(R.id.amDoneButton);
        bt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onDonePressed(view);
                    }
                }
        );
        return rootView;
    }

    public void onAddPressed (View view) {
        // Obtain Results from the fragment views
        EditText nameView = (EditText) rootView.findViewById(R.id.amNameEntry);
        String name = nameView.getText().toString();
        EditText ageView = (EditText) rootView.findViewById(R.id.amAgeEntry);
        int age;
        // Catch any invalid ages which can crash app.
        try {
            age = Integer.parseInt(ageView.getText().toString());
        } catch (NumberFormatException e) {
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(this.getActivity());
            adBuilder.setMessage("Please Enter a Valid Age").setCancelable(true);
            adBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = adBuilder.create();
            alertDialog.show();
            // Don't add to the database if age is invalid
            return;
        }
        Spinner foodView = (Spinner) rootView.findViewById(R.id.amFavFoodEntry);
        String food = foodView.getSelectedItem().toString();

        // Save Results to the database
        DatabaseHandler.getHandler().addValue(new DataEntry( name, age, food));
        onAdd.onAddCallback(name, age, food);
        nameView.setText("");
        ageView.setText("");
    }

    public void onDonePressed (View view) {
        // Obtain Results from the fragment views
        EditText nameView = (EditText) rootView.findViewById(R.id.amNameEntry);
        String name = nameView.getText().toString();
        if (!name.isEmpty()) {
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(this.getActivity());
            adBuilder.setMessage(name.toString()+ " will not be saved").setCancelable(true);
            adBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = adBuilder.create();
            alertDialog.show();
        }
        // Go back to previous fragment
        this.getActivity().getSupportFragmentManager().popBackStackImmediate();
    }
    public void onCancelPressed (View view) {
        // Go back to previous fragment
        this.getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onAdd = (OnAddListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAddListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onAdd = null;
    }
    public interface OnAddListener {
        void onAddCallback (String name, int age, String food);
    }

}
