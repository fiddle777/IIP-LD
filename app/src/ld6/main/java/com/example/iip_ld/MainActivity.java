package com.example.iip_ld;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView timeResult;
    private TextView countResult;
    private TextView characterOutput;
    private String selectedText = "";
    private Thread outputThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The root view needs padding so the toolbar stays below the status bar.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Use the blue toolbar from the layout as this activity's ActionBar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timeResult = findViewById(R.id.time_result);
        countResult = findViewById(R.id.count_result);
        characterOutput = findViewById(R.id.character_output);

        // Either of the first two rows can supply text for both context-menu actions.
        registerForContextMenu(timeResult);
        registerForContextMenu(countResult);
        timeResult.setOnClickListener(this::openContextMenu);
        countResult.setOnClickListener(this::openContextMenu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // The XML puts the exit item in the overflow and the time menu on the bar.
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.time_difference) {
            chooseTime();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void chooseTime() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(this, (view, hour, minute) -> {
            // Read the current time when the user confirms. Both times are on the same day.
            Calendar current = Calendar.getInstance();
            int currentMinutes = current.get(Calendar.HOUR_OF_DAY) * 60 + current.get(Calendar.MINUTE);
            int selectedMinutes = hour * 60 + minute;
            int difference = Math.abs(currentMinutes - selectedMinutes);

            // Use the same message for the dialog and the first text row.
            String message = getString(R.string.time_message, difference);
            timeResult.setText(message);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.time_title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), DateFormat.is24HourFormat(this));
        picker.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);

        // Remember the text from the row that opened this menu.
        selectedText = ((TextView) view).getText().toString();
        getMenuInflater().inflate(R.menu.text_actions, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.count_characters) {
            // Spaces and punctuation count too. Show the result in both places.
            String message = getString(R.string.count_message, selectedText.length());
            countResult.setText(message);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.count_title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        if (item.getItemId() == R.id.show_characters) {
            showCharacters(selectedText);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showCharacters(String text) {
        // Stop the old run before clearing the output for a new selection.
        if (outputThread != null) {
            outputThread.interrupt();
        }
        characterOutput.setText("");

        outputThread = new Thread(() -> {
            Thread worker = Thread.currentThread();
            for (int i = 0; i < text.length(); i++) {
                // Wait on the worker thread so the app stays responsive.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException stopped) {
                    return;
                }

                String character = String.valueOf(text.charAt(i));
                // Only the UI thread can change the TextView. Skip queued work from an old run.
                runOnUiThread(() -> {
                    if (outputThread == worker) {
                        characterOutput.append(character);
                    }
                });
            }
        });
        outputThread.start();
    }

    @Override
    protected void onDestroy() {
        // Stop the worker when the activity closes or is recreated.
        if (outputThread != null) {
            outputThread.interrupt();
            outputThread = null;
        }
        super.onDestroy();
    }
}
