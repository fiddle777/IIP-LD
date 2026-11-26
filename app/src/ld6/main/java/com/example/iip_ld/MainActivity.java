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
    private TextView timeResult;       // 1st row: shows the time difference result
    private TextView countResult;      // 2nd row: shows the character count result
    private TextView characterOutput;  // 3rd row: characters appear here one by one
    private String selectedText = "";  // text of the row that opened the context menu
    private Thread outputThread;       // the background thread that adds characters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the look of the screen from res/layout/activity_main.xml.
        setContentView(R.layout.activity_main);

        // The root view needs padding so the toolbar stays below the status bar.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Use the blue toolbar from the layout as this activity's ActionBar.
        // (That's where the options menu will appear.)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timeResult = findViewById(R.id.time_result);
        countResult = findViewById(R.id.count_result);
        characterOutput = findViewById(R.id.character_output);

        // Either of the first two rows can supply text for both context-menu actions.
        // registerForContextMenu = "this view has a context menu" (normally opened by a long press).
        registerForContextMenu(timeResult);
        registerForContextMenu(countResult);
        // Also open the context menu on a normal short tap, which is easier to demo.
        timeResult.setOnClickListener(this::openContextMenu);
        countResult.setOnClickListener(this::openContextMenu);
    }

    // Android calls this once to build the options menu in the toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // The XML puts the exit item in the overflow and the time menu on the bar.
        // "inflate" = turn the XML menu file into a real menu.
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    // Android calls this when an item in the options menu is tapped.
    // We check WHICH item it was by its id.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit) {
            // "Baigti" - close the app.
            finish();
            return true;
        }
        if (item.getItemId() == R.id.time_difference) {
            // "Valandų skirtumas" - open the time picker.
            chooseTime();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Opens a clock dialog. After the user picks a time, shows the difference in minutes.
    private void chooseTime() {
        // The clock dialog starts at the current time.
        Calendar now = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(this, (view, hour, minute) -> {
            // Read the current time when the user confirms. Both times are on the same day.
            // Turn both times into "minutes since midnight", then subtract.
            Calendar current = Calendar.getInstance();
            int currentMinutes = current.get(Calendar.HOUR_OF_DAY) * 60 + current.get(Calendar.MINUTE);
            int selectedMinutes = hour * 60 + minute;
            // Math.abs makes the difference positive whether the time is earlier or later.
            int difference = Math.abs(currentMinutes - selectedMinutes);

            // Use the same message for the dialog and the first text row.
            String message = getString(R.string.time_message, difference);
            timeResult.setText(message);
            // An AlertDialog is a pop-up window with a title, a message and an OK button.
            new AlertDialog.Builder(this)
                    .setTitle(R.string.time_title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), DateFormat.is24HourFormat(this));
        picker.show();
    }

    // Android calls this to build the context menu when a registered row is tapped.
    // "view" is the row that was tapped.
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);

        // Remember the text from the row that opened this menu.
        selectedText = ((TextView) view).getText().toString();
        getMenuInflater().inflate(R.menu.text_actions, menu);
    }

    // Android calls this when an item in the context menu is tapped.
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

    // Shows the text one character per second, using a background thread.
    private void showCharacters(String text) {
        // Stop the old run before clearing the output for a new selection.
        if (outputThread != null) {
            outputThread.interrupt();
        }
        characterOutput.setText("");

        // A Thread runs code "in parallel" with the rest of the app.
        // If we waited 1 second on the main (UI) thread, the whole app would freeze.
        outputThread = new Thread(() -> {
            Thread worker = Thread.currentThread();
            for (int i = 0; i < text.length(); i++) {
                // Wait on the worker thread so the app stays responsive.
                try {
                    Thread.sleep(1000); // 1000 ms = 1 second
                } catch (InterruptedException stopped) {
                    // Someone called interrupt() - stop this thread.
                    return;
                }

                String character = String.valueOf(text.charAt(i));
                // Only the UI thread can change the TextView. Skip queued work from an old run.
                // runOnUiThread sends this small piece of code to the main thread to run.
                runOnUiThread(() -> {
                    if (outputThread == worker) {
                        characterOutput.append(character);
                    }
                });
            }
        });
        // start() actually begins running the thread.
        outputThread.start();
    }

    // Runs when the screen is closed for good.
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
