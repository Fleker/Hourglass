package com.felkertech.hourglass_library_mobile.datatypes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.felkertech.hourglass_library_mobile.ConnectionUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by guest1 on 1/13/2016.
 */
public class MobileBattery
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private int batteryLevel = 0;
    private boolean registered = false;
    private Context mContext;
    private GoogleApiClient gapi;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            int level = intent.getIntExtra("level", 0);
            batteryLevel = level;
        }
    };

    public MobileBattery(Context ctx) {
        mContext = ctx;
        registerBattery();
    }
    public void unregisterBattery() {
        mContext.unregisterReceiver(this.mBatInfoReceiver);
        registered = false;
    }
    public void registerBattery() {
        if(!registered) {
            mContext.registerReceiver(this.mBatInfoReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            registered = true;
        }
        gapi = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        gapi.connect();
    }
    public int getBatteryLevel() {
        if(!(batteryLevel > 0 && batteryLevel < 100))
            batteryLevel = 50;
        return batteryLevel;
    }
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        ConnectionUtils.NodeManager nodeManager = new ConnectionUtils.NodeManager(gapi);
        nodeManager.broadcast(getBatteryLevel()+"", "mobile-battery");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
