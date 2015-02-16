package com.ece1778.keiming.assignment3.UI;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ece1778.keiming.assignment3.BackendHandlers.Database.DatabaseHandler;
import com.ece1778.keiming.assignment3.BackendHandlers.Database.TableEntry;
import com.ece1778.keiming.assignment3.R;
import com.ece1778.keiming.assignment3.UI.RecyclerView.SquareGridLayoutManager;

import java.util.ArrayList;

public class GalleryActivity extends ActionBarActivity {

    private RecyclerView mRecyclerView = null;
    private SquareGridLayoutManager mManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if (mRecyclerView == null) {
            // Setup recycler View
            mRecyclerView = (RecyclerView) findViewById(R.id.gallery_view);
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);
        }

        if (mManager == null) {
            mManager = new SquareGridLayoutManager(this, 2);
            mRecyclerView.setLayoutManager(mManager);
        }

        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(GalleryEntryAdapter.getGalleryHandler());
        }

        Toast.makeText(this, "Long press Image to delete", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
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
}
