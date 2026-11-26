package com.example.iip_ld;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {
    // "static" means there is only ONE copy of this variable for the whole app,
    // so WordCountActivity can reach it as MainActivity.wordCountIntent
    // and leave its result inside it.
    static Intent wordCountIntent = new Intent();

    // The text the user typed on the input screen.
    private String originalText = "";

    // The text label in the middle of the screen where we show results.
    private TextView resultView;

    // Opens TextInputActivity and WAITS for it to send text back.
    // The code inside "result -> { ... }" runs when that screen closes.
    private final ActivityResultLauncher<Intent> textInputLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                // RESULT_OK means the other screen finished normally and sent data back.
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Take the typed text out of the returned Intent and show it.
                    originalText = result.getData().getStringExtra(Intent.EXTRA_TEXT);
                    resultView.setText(originalText);
                }
            });

    // onCreate runs once when the screen is created. This is where we set everything up.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Load the look of the screen from res/layout/activity_main.xml.
        setContentView(R.layout.activity_main);
        // Find the text label from the layout by its id, so we can change its text.
        resultView = findViewById(R.id.tvResult);

        if (savedInstanceState != null) {
            // The screen was re-created (for example the phone was rotated).
            // Put back the text we saved in onSaveInstanceState (bottom of this file).
            originalText = savedInstanceState.getString("originalText", "");
            resultView.setText(savedInstanceState.getString("displayedText"));
        } else if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            // The app was opened because ANOTHER app "shared" text with us
            // (allowed by the SEND intent-filter in AndroidManifest.xml).
            // Show the shared text on the screen.
            CharSequence sharedText = getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                resultView.setText(sharedText);
            }
        }

        // Button 1: "Įvesti norimą tekstą" - open the text input screen.
        findViewById(R.id.btnEnterText).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TextInputActivity.class);
            // Send the current text along, so the input box is pre-filled.
            intent.putExtra(Intent.EXTRA_TEXT, originalText);
            // Open it with the launcher, because we want the typed text back.
            textInputLauncher.launch(intent);
        });

        // Button 2: "Suskaičiuoti žodžius" - open the word counting screen.
        findViewById(R.id.btnCountWords).setOnClickListener(v -> {
            // Use the shared static Intent, so the other screen can write its result into it.
            wordCountIntent = new Intent(MainActivity.this, WordCountActivity.class);
            wordCountIntent.putExtra(Intent.EXTRA_TEXT, originalText);
            startActivity(wordCountIntent);
        });

        // Button 3: "Siųsti tekstą su rezultatais" - share the text with another app
        // (Gmail, Messages, etc.).
        findViewById(R.id.btnShareText).setOnClickListener(v -> {
            // ACTION_SEND is an "implicit" Intent: we don't name a screen,
            // we just say "send this text" and Android finds apps that can do it.
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, resultView.getText().toString());
            // createChooser shows the "Share with..." list of apps.
            startActivity(Intent.createChooser(intent, null));
        });

        // Add padding so the content doesn't hide under the status bar / navigation bar.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // onResume runs every time this screen comes back to the front,
    // e.g. after the user presses Back on the word count screen.
    @Override
    protected void onResume() {
        super.onResume();
        // Check if WordCountActivity left a result in the shared Intent.
        String countedText = wordCountIntent.getStringExtra("countedText");
        if (countedText != null) {
            // Show the word count result here, then remove it so it isn't shown twice.
            resultView.setText(countedText);
            wordCountIntent.removeExtra("countedText");
        }
    }

    // Runs before the screen is destroyed and re-created (e.g. on rotation).
    // We save our text here so it is not lost.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("originalText", originalText);
        outState.putString("displayedText", resultView.getText().toString());
    }
}
