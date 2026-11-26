package com.example.iip_ld;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the look of the screen from res/layout/activity_main.xml.
        setContentView(R.layout.activity_main);

        // Add padding so the form doesn't hide under the status bar / navigation bar.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Get the form controls from activity_main.xml.
        // findViewById connects a Java variable to the element with that id in the XML.
        EditText name = findViewById(R.id.name);
        AutoCompleteTextView department = findViewById(R.id.department);
        RatingBar rating = findViewById(R.id.rating);
        TimePicker time = findViewById(R.id.time);
        DatePicker date = findViewById(R.id.date);
        Spinner city = findViewById(R.id.city);
        SwitchCompat active = findViewById(R.id.active);

        // Read the department list from strings.xml and use it for suggestions.
        String[] departments = getResources().getStringArray(R.array.departments);
        // An Adapter turns a plain list of strings into rows the drop-down can show.
        department.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, departments));
        // When a suggestion is tapped, show it in a small pop-up message (Toast).
        department.setOnItemClickListener((parent, view, position, id) ->
                Toast.makeText(this, parent.getItemAtPosition(position).toString(),
                        Toast.LENGTH_SHORT).show());

        // Show the time in 24-hour format (no AM/PM).
        time.setIs24HourView(true);
        // The lab allows ratings from one to four, so reset zero to one.
        rating.setOnRatingBarChangeListener((bar, value, fromUser) -> {
            if (value < 1) {
                bar.setRating(1);
            }
        });

        // Read the form and show the summary when Saugoti is pressed.
        findViewById(R.id.submit).setOnClickListener(view -> {
            // trim() removes extra spaces at the start and end of the name.
            String title = name.getText().toString().trim();
            // String.format builds one text from a template:
            // %s is replaced by text, %d by a whole number, %02d by a number padded to 2 digits.
            String summary = String.format(Locale.ROOT,
                    "Pavadinimas: %s; sudėtingumas: %d;\nLaikas: %02d:%02d, data: %04d-%02d-%02d"
                            + "\nPadalinys: %s; Miestas: %s; Aktyvus: %s",
                    // getMonth() counts from 0 (January = 0), so we add 1.
                    title, (int) rating.getRating(), time.getHour(), time.getMinute(),
                    date.getYear(), date.getMonth() + 1, date.getDayOfMonth(),
                    department.getText(), city.getSelectedItem(), active.isChecked() ? "Taip" : "Ne");
            // The file name is the name without spaces (and no "/"), plus ".txt".
            String fileName = title.replaceAll("\\s+", "").replace('/', '_') + ".txt";

            // A Snackbar is a message bar at the bottom of the screen. It stays for 6000 ms (6 s).
            Snackbar snackbar = Snackbar.make(view, summary, 6000);
            // Allow the whole summary to wrap onto multiple lines.
            TextView message = snackbar.getView().findViewById(
                    com.google.android.material.R.id.snackbar_text);
            message.setMaxLines(10);
            // Write the file only after the user confirms the summary.
            snackbar.setAction("Patvirtinu, informacija teisinga",
                    action -> saveSummary(fileName, summary));
            // Show the retry message if the preview runs out of time.
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar bar, int event) {
                    // DISMISS_EVENT_TIMEOUT = the 6 seconds ran out and the user did not confirm.
                    if (event == DISMISS_EVENT_TIMEOUT) {
                        Toast.makeText(MainActivity.this,
                                "Laikas baigėsi, bandykite dar kartą išsaugoti duomenis",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
            snackbar.show();
        });
    }

    // Saves the summary text into a file and tells the user where it was saved.
    private void saveSummary(String fileName, String summary) {
        // Save the summary in the app's private files folder.
        // (Only this app can see that folder. "try (...)" closes the file automatically.)
        try (FileOutputStream file = openFileOutput(fileName, MODE_PRIVATE)) {
            file.write(summary.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            // Something went wrong while writing - tell the user and stop.
            Toast.makeText(this, "Nepavyko įrašyti duomenų", Toast.LENGTH_LONG).show();
            return;
        }
        // Success - show the full path of the saved file.
        Toast.makeText(this, "Duomenys įrašyti sėkmingai:\n" + getFileStreamPath(fileName),
                Toast.LENGTH_LONG).show();
    }
}
