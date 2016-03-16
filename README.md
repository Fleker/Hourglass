# Hourglass
Boilerplate code for Android Wear watchfaces

This library comes from a lot of the watchfaces I've designed. Many have had similar interfaces and code, and I have had to copy and paste across them all. I've also wanted to tie in a lot of debug options but do so in a way that extends to all my watchfaces.

## Gradle
`compile 'com.github.fleker:hourglass:1.0.1'`
`compile 'com.github.fleker:hourglass-wear:1.0.1'`

## Mobile Preferences
A preferences activity is already included with an activity. Your mobile companion app can link to this class in an intent and have automatically synced settings.

## Wear Watchface
You can extend the `HourglassWatchface` class and override some of the methods to draw the watchface. There are methods you can use which are integrated with the Hourglass debugging system.

## Including Watchface in Manifest

    <service
            android:name="com.felkertech.sample.hourglass.PercentageWatchFace"
            android:label="@string/my_digital_name"
            android:permission="android.permission.BIND_WALLPAPER">
            <meta-data
                android:name="android.service.wallpaper"
                android:resource="@xml/hourglass_watch_face" />
            <meta-data
                android:name="com.google.android.wearable.watchface.preview"
                android:resource="@drawable/preview_digital" />
            <meta-data
                android:name="com.google.android.wearable.watchface.preview_circular"
                android:resource="@drawable/preview_digital_circular" />

            <intent-filter>
                <action android:name="android.service.wallpaper.WallpaperService" />

                <category android:name="com.google.android.wearable.watchface.category.WATCH_FACE" />
            </intent-filter>
        </service>
        
The `@xml/hourglass_watch_face` file is included in the library

### Methods

| Method Name | Return Type | Description |
| :---        | :---        | :---        |
| `isAmbient()` | `boolean` | Returns true if the watch is in ambient mode or debugging ambient mode |
| `setWatchfaceParameters(WatchFaceStyle.Builder)` | `void` | This can be called to change the position of particular UI elements in the watchface |
| `setInteractiveUpdateRateMs(long)` | `void` | This changes the update rate for the watchface drawing. In a regular watchface, this should be `1000`. If you're doing animations, this should be much smaller, such as `30`. |
| `getTaps()` | `int` | Counts the number of times the screen is tapped and then returns that amount | 
| `getNormalizedHeight(double)` | `int` | Enter a height between 0 and 360 and it will be scaled to the watch's screen size |
| `getNormalizedWidth(double)` | `int` | Enter a width between 0 and 360 and it will be scaled to the watch's screen size |
| `getHeightScale()` | `float` | Returns the scale factor for the watch's height based on an initial height of 360 |
| `getWidthScale()` | `float` | Returns the scale factor for the watch's width based on an initial width of 360 |
| `getFormattedDate()` | `String` | Returns the date formatted to the user's preferences |
| `getFormattedTime()` | `String` | Returns the time formatted to the user's preferences |
| `isCardPeeking()` | `boolean` | Returns true if a notification card is on the screen | 
| `getCardHeight()` | `int` | Returns the height of the notification card | 
| `isRound()` | `boolean` | Returns true if your watch screen is round |
| `hasChin()` | `boolean` | Returns true if your watch has a chin or a "flat tire" |
| `getChinSize()` | `int` | Returns the size of the chin in pixels |

### Additional Datatypes / Complexities
If you want to add additional features to your watchface, you can include one or more of them by including them in your watchface class.

#### WatchBattery 
To get the battery level, you can use the `WatchBattery` class.

In your `onStart` method, create a new instance and register it.
 
    mWatchBattery = new WatchBattery(getApplicationContext());
    mWatchBattery.registerBattery();

In your `onUpdate` method, you can get the battery level by calling `mWatchBattery.getBatteryLevel()` to get a percenatage of the total capacity.

Finally, you need to destroy your `WatchBattery`. In your `onEnd` method, you can call:

    mWatchBattery.unregisterBattery();