package com.ece1778.keiming.footprints.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
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
    TrackingService mService;
    private boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch mySwitch = (Switch) findViewById(R.id.locationTrackSwitch);

        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean locationPref = preferences.getBoolean("locationPref", false);
        isBound=locationPref;

         //default is false
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

                bindService(myIntent, myConnection, Context.BIND_AUTO_CREATE);
                isBound=true;
                startService(myIntent);

            }else{
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("locationPref", false); // value to store
                editor.commit();
                mService.pauseTracking();
                isBound=false;
                stopService(myIntent);
                unbindService(myConnection);
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

    private ServiceConnection myConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = ((TrackingService.SettingsBinder) service).getService();
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }

    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (isBound) {
            // Disconnect from an application service. You will no longer
            // receive calls as the service is restarted, and the service is
            // now allowed to stop at any time.
            unbindService(myConnection);
            isBound = false;
        }
    }


}
