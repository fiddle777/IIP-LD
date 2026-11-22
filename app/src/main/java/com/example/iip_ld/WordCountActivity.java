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
        setContentView(R.layout.activity_word_count);
        String originalText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        TextView countResult = findViewById(R.id.tvCountResult);
        countResult.setText(originalText);

        findViewById(R.id.btnCalculateWords).setOnClickListener(v -> {
            // Treat consecutive spaces and line breaks as a single separator.
            String trimmedText = originalText.trim();
            int words = trimmedText.isEmpty() ? 0 : trimmedText.split("\\s+").length;
            countResult.setText("Sakinyje „" + originalText + "“ esti " + words + " žodžiai");
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MainActivity.wordCountIntent.putExtra("countedText", countResult.getText().toString());
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
