package com.felkertech.hourglass_library_mobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.SeekBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.felkertech.materialpreferencesactivity.MaterialPreferencesActivity;
import com.felkertech.settingsmanager.SettingsManager;
import com.felkertech.wearsettingsmanager.WearSettingsManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by guest1 on 1/9/2016.
 */
public class SettingsActivity extends MaterialPreferencesActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private WearSettingsManager sm;
    private GoogleApiClient gapi;
    private String TAG = "SettingsActivity";

    @Override
    public void onPreferencesLoaded(final MaterialPreferencesFragment fragment) {
        Log.d(TAG, "load prefs");
        sm = (WearSettingsManager) getSettingsManager();
        gapi = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gapi.connect();

        Log.d(TAG, "Sm "+sm.toString());

        fragment.bindSummary(R.string.hourglass_date_format, RADIO_LIST_PREF, R.array.hourglass_date_format_list);
        fragment.bindSummary(R.string.hourglass_time_format, RADIO_LIST_PREF, R.array.hourglass_time_format_list);
        fragment.enablePreference(R.string.hourglass_debug_date, R.string.hourglass_debug_mode);
        fragment.enablePreference(R.string.hourglass_debug_time, R.string.hourglass_debug_mode);
        fragment.enablePreference(R.string.hourglass_ambient, R.string.hourglass_debug_mode);
        fragment.enablePreference(R.string.hourglass_debug_mode, R.string.SM_DEVELOPER);
        fragment.bindAbout(R.string.about, new DeveloperMode() {
            @Override
            public void onDeveloperModeEnabled() {
                getSettingsManager().setBoolean(R.string.SM_DEVELOPER, true);
            }
        });
        fragment.findPreference(getString(R.string.hourglass_debug_time)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialDialog timeDialog = new MaterialDialog.Builder(SettingsActivity.this)
                        .title("Change the Time")
                        .customView(R.layout.hourglass_time, false)
                        .show();
                ((SeekBar) timeDialog.getCustomView().findViewById(R.id.hour)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        sm.setInt(R.string.hourglass_debug_time_hour, progress);
                        sm.pushData();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                ((SeekBar) timeDialog.getCustomView().findViewById(R.id.minute)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        sm.setInt(R.string.hourglass_debug_time_minute, progress);
                        sm.pushData();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                ((SeekBar) timeDialog.getCustomView().findViewById(R.id.second)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        sm.setInt(R.string.hourglass_debug_time_second, progress);
                        sm.pushData();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                return false;
            }
        });
        fragment.findPreference(getString(R.string.hourglass_debug_date)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialDialog timeDialog = new MaterialDialog.Builder(SettingsActivity.this)
                        .title("Change the Time")
                        .customView(R.layout.hourglass_date, false)
                        .show();
                ((SeekBar) timeDialog.getCustomView().findViewById(R.id.year)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        sm.setInt(R.string.hourglass_debug_date_year, progress);
                        sm.pushData();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                ((SeekBar) timeDialog.getCustomView().findViewById(R.id.month)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        sm.setInt(R.string.hourglass_debug_date_month, progress);
                        sm.pushData();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                ((SeekBar) timeDialog.getCustomView().findViewById(R.id.date)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        sm.setInt(R.string.hourglass_debug_date_date, progress);
                        sm.pushData();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                return false;
            }
        });
        int[] switches = new int[]{
                R.string.hourglass_ambient,
                R.string.hourglass_analog_smooth_scroll,
                R.string.hourglass_debug_mode
        };
        for(int i: switches) {
            fragment.findPreference(getString(i)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        ((WearSettingsManager) getSettingsManager()).pushData();
                    } catch(Exception e) {
                        Log.e(TAG, "Null sm");
                    }
                    return true;
                }
            });
        }
        fragment.setRefreshTargets(new com.jenzz.materialpreference.Preference[] {
                (com.jenzz.materialpreference.Preference) fragment.findPreference(getString(R.string.hourglass_debug_mode))
        });
    }

    @Override
    public int getBackgroundColor() {
        return R.color.colorPrimary;
    }

    @Override
    public int getPreferencesXml() {
        return R.xml.hourglass_preferences;
    }

    @Override
    public SettingsManager getSettingsManager() {
        if(sm == null) {
            sm = new WearSettingsManager(this);
        }
        Log.d(TAG, "getSettingsManager");
        Log.d(TAG, sm.toString());
        return sm;
    }

    @Override
    public Activity getActivity() {
        return SettingsActivity.this;
    }

    @Override
    public Class getStringsClass() {
        return R.string.class;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected");
        sm.setSyncableSettingsManager(gapi);
        Handler h = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(TAG, "Push data");
                sm.pushData();
            }
        };
        h.sendEmptyMessageDelayed(0, 100);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Cannot connect to GPS: "+connectionResult.getErrorMessage());
    }
}
