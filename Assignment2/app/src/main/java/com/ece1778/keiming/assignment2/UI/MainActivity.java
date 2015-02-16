package com.ece1778.keiming.assignment2.UI;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ece1778.keiming.assignment2.Database.DatabaseHandler;
import com.ece1778.keiming.assignment2.R;
import com.ece1778.keiming.assignment2.UI.Fragments.AddMenu;
import com.ece1778.keiming.assignment2.UI.Fragments.MainMenu;
import com.ece1778.keiming.assignment2.classes.DataEntry;

/*
    Need a way to do the following
     1) Enter
     2) View
     3) Store
     4) Load
     5) Exit

    Need the following main ideas.
     a) ListAdapter for the Viewing of Entries
     b) Custom Save to file and Read from file system --> JSON reader and Output?
     c) Local SQL schema database --> JSON Output/Update Online Database
     d) Swipe to delete schema?
 */

public class MainActivity extends ActionBarActivity implements MainMenu.OnFragmentInteractionListener, AddMenu.OnAddListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            // Instantiate Default Fragment
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.MainContainer, MainMenu.newInstance("test1", "test2"))
                    .commit();
        }

        // Initialize the singleton database
        DatabaseHandler.initHandler(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Create the sql database that this will work with

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(View view) {
        // This is if the activity wants to override the behaviour of one of the button clicks
    }

    @Override
    public void onAddCallback(String name, int age, String food) {

    }
}
