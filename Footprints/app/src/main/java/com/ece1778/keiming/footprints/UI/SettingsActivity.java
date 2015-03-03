package com.ece1778.keiming.footprints.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.ece1778.keiming.footprints.R;
import com.ece1778.keiming.footprints.Services.TrackingService;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch mySwitch = (Switch) findViewById(R.id.locationTrackSwitch);

        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean locationPref = preferences.getBoolean("locationPref", false);  //default is false
        if (locationPref)
        {
            mySwitch.setChecked(true);
        }
        else
        {
            mySwitch.setChecked(false);
        }

        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
            Intent myIntent = new Intent(SettingsActivity.this,TrackingService.class);

            if(isChecked){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("locationPref", true); // value to store
                editor.commit();
                startService(myIntent);

            }else{
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("locationPref", false); // value to store
                editor.commit();
                stopService(myIntent);
            }

            }
        });

        Intent myIntent = new Intent(SettingsActivity.this,TrackingService.class);
        //check the current state to start or stop Tracking Service
        if(mySwitch.isChecked()){
            startService(myIntent);
        }
        else {
            stopService(myIntent);
        }
    }


}
