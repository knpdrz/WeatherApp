package com.hrmaarhus.weatherapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static com.hrmaarhus.weatherapp.utils.Globals.CONNECT;

/**
 * Created by hulda on 6.11.2017.
 */

public class CheckNetworkConnection {
    //Inspired by Kasper's NetworkChecker class in WeatherServiceDemo project
    public static Boolean isDeviceConnected(Context c){
        ConnectivityManager connectMan = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            //There is a some type of connection
            Log.d(CONNECT, "Got connections" +
                    netInfo.toString()
            );

            return true;
        } else {
            //No type of network connection
            Log.d(CONNECT, "No connections");
            return false;
        }
    }
}
