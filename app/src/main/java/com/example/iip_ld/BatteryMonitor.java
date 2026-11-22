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
    private static final int LOW_BATTERY_PERCENT = 20;
    private static final String CHANNEL_ID = "battery_alerts";
    private static final int ALERT_ID = 1;

    private SharedPreferences settings;
    private boolean monitoring;
    private boolean alertShown;
    int batteryPercent = -1;
    Runnable onLevelChanged;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!monitoring) {
                return;
            }

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level < 0 || scale <= 0) {
                return;
            }

            batteryPercent = Math.round(level * 100f / scale);
            if (onLevelChanged != null) {
                onLevelChanged.run();
            }
            checkBattery();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences("battery_monitor", MODE_PRIVATE);
        if (settings.getBoolean("monitoring", false)) {
            setMonitoring(true);
        }
    }

    boolean isMonitoring() {
        return monitoring;
    }

    void setMonitoring(boolean enabled) {
        if (monitoring == enabled) {
            return;
        }

        monitoring = enabled;
        settings.edit().putBoolean("monitoring", enabled).apply();
        if (enabled) {
            // Listen even when the activity is no longer on screen.
            ContextCompat.registerReceiver(this, batteryReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    ContextCompat.RECEIVER_EXPORTED);
        } else {
            unregisterReceiver(batteryReceiver);
            batteryPercent = -1;
            alertShown = false;
            NotificationManagerCompat.from(this).cancel(ALERT_ID);
        }
    }

    void checkBattery() {
        if (!monitoring || batteryPercent < 0) {
            return;
        }
        if (batteryPercent > LOW_BATTERY_PERCENT) {
            alertShown = false;
            NotificationManagerCompat.from(this).cancel(ALERT_ID);
            return;
        }
        if (alertShown || !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.battery_alerts), NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Intent openApp = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openActivity = PendingIntent.getActivity(this, 0, openApp,
                PendingIntent.FLAG_IMMUTABLE);

        // A quiet notification leaves the screen off and opens the app only on tap.
        NotificationManagerCompat.from(this).notify(ALERT_ID,
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(getString(R.string.low_battery))
                        .setContentText(getString(R.string.battery_level, batteryPercent))
                        .setContentIntent(openActivity)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setSilent(true)
                        .setAutoCancel(true)
                        .build());
        alertShown = true;
    }
}
