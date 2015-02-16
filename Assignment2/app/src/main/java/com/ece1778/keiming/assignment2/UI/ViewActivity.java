package com.ece1778.keiming.assignment2.UI;

import android.app.ListActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ece1778.keiming.assignment2.R;
import com.ece1778.keiming.assignment2.UI.Fragments.AddMenu;
import com.ece1778.keiming.assignment2.UI.Fragments.ViewMenu;


public class ViewActivity extends ActionBarActivity implements AddMenu.OnAddListener{

    ViewMenu viewMenuFrag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        if (savedInstanceState == null) {
            // Instantiate Default Fragment
            viewMenuFrag = ViewMenu.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.ViewContainer, viewMenuFrag)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            // Switch this to the add fragment
            FragmentManager fm = this.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment newFrag = AddMenu.newInstance();
            ft.replace(R.id.ViewContainer, newFrag);
            ft.addToBackStack("Switch to Add Fragment");
            ft.commit();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onAddCallback(String name, int age, String food) {
        viewMenuFrag.addEntry(name, age, food);
    }
}
