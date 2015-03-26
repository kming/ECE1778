package com.ece1778.footprints.database;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Don Zhu on 24/03/2015.
 */
public class Vector {

    private LatLng p = new LatLng(0,0);
    private LatLng q = new LatLng(0,0);


    public Vector(LatLng a, LatLng b){
        p=a;
        q=b;
    }

    public Vector(){
        p=new LatLng(0,0);
        q=new LatLng(0,0);
    }

    public double giveLength ( ){
        double xDiff=q.latitude - p.latitude;
        double yDiff=q.longitude - p.longitude;
        return Math.sqrt( (xDiff*xDiff)+(yDiff*yDiff) );
    }

}
