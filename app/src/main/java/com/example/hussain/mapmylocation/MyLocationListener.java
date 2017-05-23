package com.example.hussain.mapmylocation;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Hussain on 21-May-17.
 */

public class MyLocationListener implements LocationListener {
    MainActivity main;
    public MyLocationListener(MainActivity mainInterface)
    {
        this.main = mainInterface;
    }
    public void onLocationChanged(Location location) {
        main.onLocationChanged(location);
    }
    public void onProviderDisabled(String provider) {
        main.requestGPS(provider);
    }
    public void onProviderEnabled(String provider) {

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(status == LocationProvider.OUT_OF_SERVICE) {
            main.switchProvider(provider);
        }
    }
}
