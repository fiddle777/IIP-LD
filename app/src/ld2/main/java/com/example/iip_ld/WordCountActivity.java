package com.example.iip_ld;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.OnBackPressedCallback;
public class WordCountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Load the look of this screen from res/layout/activity_word_count.xml.
        setContentView(R.layout.activity_word_count);
        // Take the text out of the Intent that opened this screen.
        String originalText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        TextView countResult = findViewById(R.id.tvCountResult);
        // At first just show the text itself.
        countResult.setText(originalText);

        // Button "Paskaičiuok žodžius": count the words and show the sentence with the count.
        findViewById(R.id.btnCalculateWords).setOnClickListener(v -> {
            // Treat consecutive spaces and line breaks as a single separator.
            // trim() removes spaces at the start and end.
            String trimmedText = originalText.trim();
            // split("\\s+") cuts the text into pieces wherever there are spaces,
            // so the number of pieces = the number of words. Empty text = 0 words.
            int words = trimmedText.isEmpty() ? 0 : trimmedText.split("\\s+").length;
            countResult.setText("Sakinyje „" + originalText + "“ esti " + words + " žodžiai");
        });

        // When Back is pressed: leave the result in the shared Intent, then close.
        // MainActivity reads it in its onResume() method.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MainActivity.wordCountIntent.putExtra("countedText", countResult.getText().toString());
                finish();
            }
        });

        // Add padding so the content doesn't hide under the system bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
