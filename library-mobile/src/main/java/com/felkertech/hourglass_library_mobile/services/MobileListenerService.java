package com.felkertech.hourglass_library_mobile.services;

import android.app.NotificationManager;

import com.felkertech.hourglass_library_mobile.datatypes.MobileBattery;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by guest1 on 1/10/2016.
 */
public class MobileListenerService extends WearableListenerService {
    private int NOTIFICATION_ID = 12;
    private MobileBattery mb;
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String message = new String(messageEvent.getData());

        if(message.contains("enable-mobile-battery")) {
            mb = new MobileBattery(this);
            mb.registerBattery();
        } else if(message.contains("disable-mobile-battery")) {
            mb.unregisterBattery();
        }
    }
}
