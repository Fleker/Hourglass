package com.felkertech.hourglass;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.Time;
import android.util.Log;

import com.felkertech.hourglass_library_wear.HourglassWatchface;
import com.felkertech.settingsmanager.wear.SettingsManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by guest1 on 1/10/2016.
 */
public class PercentageWatchFace extends HourglassWatchface {
    private Paint mTextPaint;
    private Paint mSmallTextPaint;
    private Paint mBluePaint;
    private Paint mRedPaint;
    private String TAG = "PercentageWatchFace";

    private Paint mFadePaint;
    private int OVAL_ALPHA = 32;

    @Override
    public void onStart() {
        mTextPaint = new Paint();
        mTextPaint.setColor(getResources().getColor(android.R.color.white));
        mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        mTextPaint.setTextSize(22);
        mSmallTextPaint = new Paint();
        mSmallTextPaint.set(mTextPaint);
        mSmallTextPaint.setTextSize(10);
        setInteractiveUpdateRateMs(1000);

        mBluePaint = new Paint();
        mBluePaint.setColor(getResources().getColor(android.R.color.holo_blue_light));
        mRedPaint = new Paint();
        mRedPaint.setColor(getResources().getColor(android.R.color.holo_red_light));

        mFadePaint = new Paint();
        mFadePaint.setColor(getResources().getColor(android.R.color.holo_purple));
        mFadePaint.setStrokeCap(Paint.Cap.ROUND);
        mFadePaint.setAlpha(OVAL_ALPHA);
    }

    @Override
    public void onEnd() {

    }

    @Override
    public void onTap(int taps, int x, int y) {
        switch(taps%3) {
            case 0:
                mFadePaint.setColor(getResources().getColor(android.R.color.holo_purple));
                break;
            case 1:
                mFadePaint.setColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case 2:
                mFadePaint.setColor(getResources().getColor(android.R.color.holo_orange_light));
                break;
        }
        mFadePaint.setAlpha(OVAL_ALPHA);
    }

    @Override
    public void onUpdate(Canvas canvas, Time mDate) {
        canvas.drawColor(getResources().getColor(android.R.color.black));
//        double datePercent = 100d*(mDate.toMillis(false)%(1000d*60d*60d*24d))/(1000d*60d*60d*24d);
        double datePercent = 100d*(mDate.hour*1000*60*60 + mDate.minute*1000*60 + mDate.second*1000)/(1000d*60d*60d*24d);
        Log.d(TAG, mDate.toMillis(false)+".:");
        Log.d(TAG, mDate.hour+":"+mDate.minute+":"+mDate.second);
        int x = getNormalizedWidth(136);
        int y = getNormalizedHeight(120);
//        Log.d(TAG, datePercent+"%");
        int percentX = getNormalizedWidth(datePercent*3.6);
//        Log.d(TAG, percentX+"px");
        SettingsManager sm = new SettingsManager(getApplicationContext());
        Log.d(TAG, sm.getBoolean(com.felkertech.hourglass_library_wear.R.string.hourglass_debug_mode)+"<Debug  "+sm.getBoolean(R.string.hourglass_ambient)+"<Ambient "+ sm.getInt(com.felkertech.hourglass_library_wear.R.string.hourglass_debug_time_hour)+"<Hour");

        if(isAmbient()) {
            canvas.drawLine(0, getNormalizedHeight(180), getNormalizedWidth(360), getNormalizedHeight(180), mTextPaint);
            canvas.drawRect(percentX-12*getWidthScale(), getNormalizedHeight(176), percentX+12*getWidthScale(), getNormalizedHeight(184), mTextPaint);
        } else {
            canvas.drawLine(0, getNormalizedHeight(180), percentX, getNormalizedHeight(180), mBluePaint);
            canvas.drawLine(percentX, getNormalizedHeight(180), getNormalizedWidth(360), getNormalizedHeight(180), mRedPaint);

            Paint mFadeLinePaint = new Paint();
            mFadeLinePaint.set(mFadePaint);
            mFadeLinePaint.setAlpha(223);
            canvas.drawLine(percentX-160*getWidthScale(), getNormalizedHeight(180), percentX+80*getWidthScale(), getNormalizedHeight(180), mFadePaint);
            canvas.drawLine(percentX-128*getWidthScale(), getNormalizedHeight(180), percentX+64*getWidthScale(), getNormalizedHeight(180), mFadePaint);
            canvas.drawLine(percentX-96*getWidthScale(), getNormalizedHeight(180), percentX+48*getWidthScale(), getNormalizedHeight(180), mFadePaint);
            canvas.drawLine(percentX-80*getWidthScale(), getNormalizedHeight(180), percentX+40*getWidthScale(), getNormalizedHeight(180), mFadePaint);

            canvas.drawOval(percentX-12*getWidthScale(), getNormalizedHeight(176), percentX+12*getWidthScale(), getNormalizedHeight(184), mFadePaint);
            canvas.drawOval(percentX-24*getWidthScale(), getNormalizedHeight(172), percentX+24*getWidthScale(), getNormalizedHeight(188), mFadePaint);
            canvas.drawOval(percentX-48*getWidthScale(), getNormalizedHeight(164), percentX+48*getWidthScale(), getNormalizedHeight(196), mFadePaint);
            canvas.drawOval(percentX-72*getWidthScale(), getNormalizedHeight(148), percentX+72*getWidthScale(), getNormalizedHeight(212), mFadePaint);
        }
        NumberFormat formatter = new DecimalFormat("#0.00");
        canvas.drawText(formatter.format(datePercent)+"%", x, y, mTextPaint);
        canvas.drawText(getFormattedDate(), x, y-12*getHeightScale(), mSmallTextPaint);
    }
}
