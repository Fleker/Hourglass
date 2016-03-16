package com.felkertech.hourglass_library_wear.datatypes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.felkertech.hourglass_library_wear.ConnectionUtils;
import com.felkertech.hourglass_library_wear.R;
import com.felkertech.settingsmanager.wear.SettingsManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by guest1 on 1/13/2016.
 */
public class MobileBattery
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private Context mContext;
    private GoogleApiClient gapi;
    public MobileBattery(Context context) {
        mContext = context;
    }
    public void registerBattery() {
        gapi = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gapi.connect();
    }
    public int getBatteryLevel() {
        SettingsManager sm = new SettingsManager(mContext);
        return sm.getInt(R.string.hourglass_data_phone_battery);
    }

    public void unregisterBattery() {
        ConnectionUtils.NodeManager nm = new ConnectionUtils.NodeManager(gapi);
        nm.broadcast("disable-mobile-battery", "datatypes");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        ConnectionUtils.NodeManager nm = new ConnectionUtils.NodeManager(gapi);
        nm.broadcast("enable-mobile-battery", "datatypes");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
