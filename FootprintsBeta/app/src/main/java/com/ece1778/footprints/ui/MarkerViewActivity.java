package com.ece1778.footprints.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ece1778.footprints.BuildConfig;
import com.ece1778.footprints.R;
import com.ece1778.footprints.database.MarkerDBManager;
import com.ece1778.footprints.database.MarkerTableEntry;
import com.ece1778.footprints.manager.MarkerScrollAdapter;

import java.util.ArrayList;
import java.util.List;

public class MarkerViewActivity extends ActionBarActivity {
private static final String TAG = MarkerViewActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_view);

        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        recList.setLayoutManager(llm);

        MarkerScrollAdapter msa = new MarkerScrollAdapter(createList(5));
        recList.setAdapter(msa);
    }

    private List<MarkerTableEntry> createList(int size) {

        List<MarkerTableEntry> savedEntries = MarkerDBManager.getManager(this).getAllValues();
        List<MarkerTableEntry> result = new ArrayList<MarkerTableEntry>();

        for (MarkerTableEntry entry:savedEntries) {
            MarkerTableEntry marker = new MarkerTableEntry();
            marker.setTitle(entry.getTitle());
            marker.setNote(entry.getNote());
            marker.setPicture(entry.getPicture());

            result.add(marker);

        }

        return result;
    }

}
