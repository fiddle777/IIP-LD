package com.example.iip_ld;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the look of the screen from res/layout/activity_main.xml.
        setContentView(R.layout.activity_main);

        // Add padding so the content doesn't hide under the system bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Android restores the list and result history when recreating the activity.
        // So we only add the list the very first time (otherwise we'd get two lists after rotating).
        if (savedInstanceState == null) {
            // A "transaction" is a group of fragment changes applied together with commit().
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.list_container, new TextListFragment())
                    .commit();
        }
    }
}
