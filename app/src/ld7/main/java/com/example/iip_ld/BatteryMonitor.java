package com.example.iip_ld;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
public class BatteryMonitor extends Application {
    private static final int LOW_BATTERY_PERCENT = 20;          // "low battery" limit
    private static final String CHANNEL_ID = "battery_alerts";  // id of our notification channel
    private static final int ALERT_ID = 1;                      // id of our notification

    private SharedPreferences settings; // remembers if monitoring is on, even after restart
    private boolean monitoring;         // is monitoring switched on right now?
    private boolean alertShown;         // did we already show the low battery notification?
    int batteryPercent = -1;            // last known battery %, -1 = not known yet
    Runnable onLevelChanged;            // code to run when the % changes (MainActivity sets it)

    // The receiver: its onReceive() runs every time the battery broadcast arrives.
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!monitoring) {
                return;
            }

            // The broadcast tells us the level (e.g. 45) and the scale (usually 100).
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level < 0 || scale <= 0) {
                return; // bad data, ignore it
            }

            // Turn it into a percentage.
            batteryPercent = Math.round(level * 100f / scale);
            // Tell the screen (if it's open) to update the text.
            if (onLevelChanged != null) {
                onLevelChanged.run();
            }
            // Check if we need to show the low battery notification.
            checkBattery();
        }
    };

    // Runs once when the app process starts (before any screen).
    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences("battery_monitor", MODE_PRIVATE);
        // If monitoring was ON last time, switch it ON again automatically.
        if (settings.getBoolean("monitoring", false)) {
            setMonitoring(true);
        }
    }

    boolean isMonitoring() {
        return monitoring;
    }

    // Turns battery monitoring on or off (called by the switch on the screen).
    void setMonitoring(boolean enabled) {
        if (monitoring == enabled) {
            return; // nothing changes
        }

        monitoring = enabled;
        // Save the choice so it is remembered next time the app starts.
        settings.edit().putBoolean("monitoring", enabled).apply();
        if (enabled) {
            // Listen even when the activity is no longer on screen.
            // "Register" = tell Android: send me the ACTION_BATTERY_CHANGED broadcasts.
            ContextCompat.registerReceiver(this, batteryReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    ContextCompat.RECEIVER_EXPORTED);
        } else {
            // Stop listening, forget the level, and remove our notification if it's showing.
            unregisterReceiver(batteryReceiver);
            batteryPercent = -1;
            alertShown = false;
            NotificationManagerCompat.from(this).cancel(ALERT_ID);
        }
    }

    // Decides whether to show the low battery notification.
    void checkBattery() {
        if (!monitoring || batteryPercent < 0) {
            return;
        }
        // Battery is fine (above 20%): remove the notification and reset, so it can show again later.
        if (batteryPercent > LOW_BATTERY_PERCENT) {
            alertShown = false;
            NotificationManagerCompat.from(this).cancel(ALERT_ID);
            return;
        }
        // Already shown once, or the user turned notifications off -> do nothing.
        if (alertShown || !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            return;
        }
        // Android 13+ (TIRAMISU) needs the POST_NOTIFICATIONS permission from the user.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Android 8+ (O) requires every notification to belong to a "channel".
        // The user can see/turn off channels in the app's settings.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.battery_alerts), NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        // What happens when the notification is tapped: open MainActivity.
        // A PendingIntent is an Intent we give to Android to use LATER, on our behalf.
        Intent openApp = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openActivity = PendingIntent.getActivity(this, 0, openApp,
                PendingIntent.FLAG_IMMUTABLE);

        // A quiet notification leaves the screen off and opens the app only on tap.
        // Build the notification: icon, title, text, what to open on tap, and show it.
        NotificationManagerCompat.from(this).notify(ALERT_ID,
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(getString(R.string.low_battery))
                        .setContentText(getString(R.string.battery_level, batteryPercent))
                        .setContentIntent(openActivity)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setSilent(true)
                        .setAutoCancel(true)  // disappears after it's tapped
                        .build());
        alertShown = true;
    }
}
