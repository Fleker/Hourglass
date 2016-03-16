package com.felkertech.hourglass_library_wear.datatypes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by guest1 on 1/12/2016.
 */
public class WatchBattery {
    int batteryLevel = 0;
    boolean registered = false;
    Context mContext;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            int level = intent.getIntExtra("level", 0);
            batteryLevel = level;
        }
    };

    public WatchBattery(Context ctx) {
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
    }
    public int getBatteryLevel() {
        if(!(batteryLevel > 0 && batteryLevel < 100))
            batteryLevel = 50;
        return batteryLevel;
    }
    public boolean isRegistered() {
        return registered;
    }
}