package com.felkertech.hourglass;

import android.graphics.drawable.Drawable;

import com.felkertech.hourglass_library_mobile.HourglassLaunchActivity;
import com.felkertech.hourglass_library_mobile.SettingsActivity;

public class MainActivity extends HourglassLaunchActivity {

    @Override
    public String getIssueTracker() {
        return "https://github.com/Fleker/Hourglass";
    }

    @Override
    public Class getSettingsActivity() {
        return SettingsActivity.class;
    }

    @Override
    public Drawable getWatchfacePreview() {
        return getResources().getDrawable(R.drawable.common_google_signin_btn_icon_dark);
    }
}
