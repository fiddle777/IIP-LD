package com.example.iip_ld;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
public class MainActivity extends AppCompatActivity {
    private BatteryMonitor monitor;  // our Application object that does the monitoring
    private TextView batteryLevel;   // the text that shows the battery %

    // Asks the user "Allow this app to send notifications?" (needed on Android 13+).
    // The code inside runs after the user answers.
    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    // Allowed - check right away if a low battery notification is needed.
                    monitor.checkBattery();
                } else {
                    // Not allowed - tell the user how to turn notifications on.
                    showNotificationsDisabled();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the look of the screen from res/layout/activity_main.xml.
        setContentView(R.layout.activity_main);

        // getApplication() gives us the one BatteryMonitor object of the app.
        monitor = (BatteryMonitor) getApplication();
        batteryLevel = findViewById(R.id.battery_level);
        SwitchCompat monitoringSwitch = findViewById(R.id.monitoring_switch);
        // Make the switch match the current state (it may already be ON from last time).
        monitoringSwitch.setChecked(monitor.isMonitoring());
        // Runs when the user flips the switch.
        monitoringSwitch.setOnCheckedChangeListener((button, checked) -> {
            monitor.setMonitoring(checked);
            showBatteryLevel();
            // Turned ON but notifications are not allowed -> ask for permission (or explain).
            if (checked && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    showNotificationsDisabled();
                }
            }
        });
    }

    // Runs when the screen becomes visible.
    @Override
    protected void onStart() {
        super.onStart();
        // Ask BatteryMonitor to call showBatteryLevel() whenever the % changes.
        monitor.onLevelChanged = this::showBatteryLevel;
        showBatteryLevel();
        monitor.checkBattery();
    }

    // Runs when the screen is no longer visible.
    @Override
    protected void onStop() {
        // The screen is hidden, so there's nothing to update - stop the callbacks.
        monitor.onLevelChanged = null;
        super.onStop();
    }

    // Updates the text: "monitoring off", "waiting for data", or "Battery level: X%".
    private void showBatteryLevel() {
        if (!monitor.isMonitoring()) {
            batteryLevel.setText(R.string.monitoring_off);
        } else if (monitor.batteryPercent < 0) {
            batteryLevel.setText(R.string.waiting_for_level);
        } else {
            batteryLevel.setText(getString(R.string.battery_level, monitor.batteryPercent));
        }
    }

    // Small pop-up telling the user to enable notifications in settings.
    private void showNotificationsDisabled() {
        Toast.makeText(this, R.string.notifications_disabled, Toast.LENGTH_LONG).show();
    }
}
