package com.example.iip_ld;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.OnBackPressedCallback;
public class TextInputActivity extends AppCompatActivity {
    // The text box where the user types.
    private EditText textInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Load the look of this screen from res/layout/activity_text_input.xml.
        setContentView(R.layout.activity_text_input);
        textInput = findViewById(R.id.etInputText);
        // Pre-fill the box with the text MainActivity sent us (so the user can edit it).
        textInput.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
        // The button sends the text back.
        findViewById(R.id.btnConfirmText).setOnClickListener(v -> returnText());

        // Pressing the phone's Back button ALSO sends the text back
        // (instead of just closing the screen and losing the text).
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnText();
            }
        });

        // Add padding so the content doesn't hide under the system bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Packs the typed text into an Intent, gives it back to MainActivity, and closes this screen.
    private void returnText() {
        Intent result = new Intent();
        result.putExtra(Intent.EXTRA_TEXT, textInput.getText().toString());
        // RESULT_OK tells MainActivity "everything went fine, here is the data".
        setResult(RESULT_OK, result);
        // Close this screen and go back to MainActivity.
        finish();
    }
}
