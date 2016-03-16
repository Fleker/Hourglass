/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.felkertech.hourglass_library_wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.felkertech.settingsmanager.wear.SettingsManager;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public abstract class HourglassWatchface extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    protected SettingsManager sm;

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    private Engine mEngine;

    @Override
    public Engine onCreateEngine() {
        return mEngine = new Engine();
    }

    public void setInteractiveUpdateRateMs(long rate) {
        INTERACTIVE_UPDATE_RATE_MS = rate;
    }

    public abstract void onStart();
    public abstract void onEnd();
    public abstract void onTap(int taps, int x, int y);
    public abstract void onUpdate(Canvas canvas, Time mDate);

    public boolean isAmbient() {
        return mEngine.mAmbient || sm.getBoolean(R.string.hourglass_ambient);
    }
    public void setWatchfaceParameters(WatchFaceStyle.Builder builder) {
        mEngine.setWatchFaceStyle(builder.build());
    }
    public int getTaps() {
        return mEngine.mTapCount;
    }
    public int getNormalizedHeight(double height) {
        return (int) (height/360*mEngine.mCanvas.getHeight());
    }
    public int getNormalizedWidth(double width) {
        return (int) (width/360*mEngine.mCanvas.getWidth());
    }
    public float getWidthScale() {
        return mEngine.mCanvas.getWidth()/360f;
    }
    public float getHeightScale() {
        return mEngine.mCanvas.getHeight()/360f;
    }
    public String getFormattedDate() {
        Date d = new Date(mEngine.mTime.toMillis(false));
        SimpleDateFormat sdf = new SimpleDateFormat(sm.getString(R.string.hourglass_date_format));
        return sdf.format(d);
    }
    public String getFormattedTime() {
        Date d = new Date(mEngine.mTime.toMillis(false));
        SimpleDateFormat sdf = new SimpleDateFormat(sm.getString(R.string.hourglass_time_format));
        return sdf.format(d);
    }
    public boolean isCardPeeking() {
        return getCardHeight() > 0;
    }
    public int getCardHeight() {
        return mEngine.getPeekCardPosition().height();
    }
    public boolean isRound() {
        return mEngine.mIsRound;
    }
    public boolean hasChin() {
        return getChinSize() > 0;
    }
    public int getChinSize() {
        return mEngine.mChinSize;
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<Engine> mWeakReference;

        public EngineHandler(HourglassWatchface.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            HourglassWatchface.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mTextPaint;
        boolean mAmbient;
        Time mTime;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        int mTapCount;
        Canvas mCanvas;
        boolean mIsRound;
        int mChinSize;
        private String TAG = "hourglass_watch";

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(HourglassWatchface.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            mTime = new Time();
            sm = new SettingsManager(getApplicationContext());
            Log.d(TAG, "Started2");
            onStart();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            onEnd();
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            HourglassWatchface.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            HourglassWatchface.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = HourglassWatchface.this.getResources();
            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case WatchFaceService.TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case WatchFaceService.TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    mTapCount++;
                    onTap(mTapCount, x, y);
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Log.d(TAG, "onDraw2");
            sm = new SettingsManager(getApplicationContext());
            mCanvas = canvas;
            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            Log.d(TAG, sm.getBoolean(R.string.hourglass_debug_mode)+"<Debug");
            Log.d(TAG, sm.getInt(R.string.hourglass_debug_time_hour)+"<Hour");
            if(sm.getBoolean(R.string.hourglass_debug_mode)) {
                mTime.set(
                        sm.getInt(R.string.hourglass_debug_time_second),
                        sm.getInt(R.string.hourglass_debug_time_minute),
                        sm.getInt(R.string.hourglass_debug_time_hour),
                        sm.getInt(R.string.hourglass_debug_date_date),
                        sm.getInt(R.string.hourglass_debug_date_month),
                        sm.getInt(R.string.hourglass_debug_date_year)
                );
            } else {
                mTime.setToNow();
            }
            // Draw the background.
            if (isAmbient()) {
                canvas.drawColor(Color.BLACK);
            }
            onUpdate(canvas, mTime);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isAmbient();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
            mChinSize = insets.getSystemWindowInsetBottom();
        }
    }
}
