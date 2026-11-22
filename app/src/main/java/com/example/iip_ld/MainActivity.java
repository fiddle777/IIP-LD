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
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        EditText name = findViewById(R.id.name);
        AutoCompleteTextView department = findViewById(R.id.department);
        RatingBar rating = findViewById(R.id.rating);
        TimePicker time = findViewById(R.id.time);
        DatePicker date = findViewById(R.id.date);
        Spinner city = findViewById(R.id.city);
        SwitchCompat active = findViewById(R.id.active);

        String[] departments = getResources().getStringArray(R.array.departments);
        department.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, departments));
        department.setOnItemClickListener((parent, view, position, id) ->
                Toast.makeText(this, parent.getItemAtPosition(position).toString(),
                        Toast.LENGTH_SHORT).show());

        time.setIs24HourView(true);
        rating.setOnRatingBarChangeListener((bar, value, fromUser) -> {
            if (value < 1) {
                bar.setRating(1);
            }
        });

        findViewById(R.id.submit).setOnClickListener(view -> {
            String title = name.getText().toString().trim();
            String summary = String.format(Locale.ROOT,
                    "Pavadinimas: %s; sudėtingumas: %d;\nLaikas: %02d:%02d, data: %04d-%02d-%02d"
                            + "\nPadalinys: %s; Miestas: %s; Aktyvus: %s",
                    title, (int) rating.getRating(), time.getHour(), time.getMinute(),
                    date.getYear(), date.getMonth() + 1, date.getDayOfMonth(),
                    department.getText(), city.getSelectedItem(), active.isChecked() ? "Taip" : "Ne");
            String fileName = title.replaceAll("\\s+", "").replace('/', '_') + ".txt";

            Snackbar snackbar = Snackbar.make(view, summary, 6000);
            // Allow the whole summary to wrap onto multiple lines.
            TextView message = snackbar.getView().findViewById(
                    com.google.android.material.R.id.snackbar_text);
            message.setMaxLines(10);
            snackbar.setAction("Patvirtinu, informacija teisinga",
                    action -> saveSummary(fileName, summary));
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar bar, int event) {
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

    private void saveSummary(String fileName, String summary) {
        try (FileOutputStream file = openFileOutput(fileName, MODE_PRIVATE)) {
            file.write(summary.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Toast.makeText(this, "Nepavyko įrašyti duomenų", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Duomenys įrašyti sėkmingai:\n" + getFileStreamPath(fileName),
                Toast.LENGTH_LONG).show();
    }
}
