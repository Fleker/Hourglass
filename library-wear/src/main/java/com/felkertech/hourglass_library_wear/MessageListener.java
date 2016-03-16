package com.felkertech.hourglass_library_wear;

import android.util.Log;

import com.felkertech.settingsmanager.wear.SettingsManager;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Nick on 3/15/2016.
 */
public class MessageListener extends WearableListenerService {
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("PreferenceHourglass", dataEvents.toString());
        SettingsManager sm = new SettingsManager(getApplicationContext());
        sm.pullData(dataEvents);
    }
}
