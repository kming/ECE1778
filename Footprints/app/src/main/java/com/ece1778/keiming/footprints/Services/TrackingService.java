package com.ece1778.keiming.footprints.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ece1778.keiming.footprints.BuildConfig;
import com.ece1778.keiming.footprints.Classes.LocTableEntry;
import com.ece1778.keiming.footprints.Managers.LocationDBManager;
import com.ece1778.keiming.footprints.Managers.LocationManager;
import com.ece1778.keiming.footprints.Utils.GeneralUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Timestamp;

/**
 * Created by Don Zhu on 03/03/2015.
 */
public class TrackingService extends Service {
    private static final String TAG ="TrackingService";
    //private GoogleMap mMap;
    private boolean isRunning = false;

    @Override
    public void onCreate(){
        super.onCreate();
        if (BuildConfig.DEBUG){Log.d(TAG, "Service onCreate");}
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (BuildConfig.DEBUG){Log.d(TAG,"Service onStartCommand");}

        new Thread(new Runnable(){
            @Override
            public void run(){
                //location tracking code here
                for (int i = 0; i < 20; i++) {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                    }
                    if (isRunning) {
                        if (BuildConfig.DEBUG) {Log.d(TAG, "Service running");}
                        //Location location= LocationManager.getManager(TrackingService.this).getLocation();
                        //LatLng curLoc = new LatLng(location.getLatitude(), location.getLongitude());
                        //Toast.makeText(null, "Location:" + curLoc, Toast.LENGTH_SHORT).show();
                    }

                }stopSelf();
            }
        }).start();

        return Service.START_STICKY;
    }

    public class SettingsBinder extends Binder {
        public TrackingService getService() {
            return TrackingService.this;
        }
    }
    private final IBinder myBinder = new SettingsBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return myBinder;
        //if (BuildConfig.DEBUG){Log.d(TAG,"Service onBind");}
        //return null;
    }
    public void pauseTracking() {
        if(isRunning) {
            //pause tracking
        }

    }


    @Override
    public void onDestroy(){
        isRunning = false;
        if (BuildConfig.DEBUG){Log.d(TAG,"Service onDestroy");}
        super.onDestroy();
    }
}
