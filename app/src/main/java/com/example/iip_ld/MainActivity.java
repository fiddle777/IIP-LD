package com.example.iip_ld;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    private BatteryMonitor monitor;
    private TextView batteryLevel;
    private final ActivityLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    monitor.checkBattery();
                } else {
                    showNotificationsDisabled();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        monitor = (BatteryMonitor) getApplication();
        batteryLevel = findViewById(R.id.battery_level);
        SwitchCompat monitoringSwitch = findViewById(R.id.monitoring_switch);
        monitoringSwitch.setChecked(monitor.isMonitoring());
        monitoringSwitch.setOnCheckedChangeListener((button, checked) -> {
            monitor.setMonitoring(checked);
            showBatteryLevel();
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

    @Override
    protected void onStart() {
        super.onStart();
        monitor.onLevelChanged = this::showBatteryLevel;
        showBatteryLevel();
        monitor.checkBattery();
    }

    @Override
    protected void onStop() {
        monitor.onLevelChanged = null;
        super.onStop();
    }

    private void showBatteryLevel() {
        if (!monitor.isMonitoring()) {
            batteryLevel.setText(R.string.monitoring_off);
        } else if (monitor.batteryPercent < 0) {
            batteryLevel.setText(R.string.waiting_for_level);
        } else {
            batteryLevel.setText(getString(R.string.battery_level, monitor.batteryPercent));
        }
    }

    private void showNotificationsDisabled() {
        Toast.makeText(this, R.string.notifications_disabled, Toast.LENGTH_LONG).show();
    }
}
