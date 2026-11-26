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
    // The lab also requires a shared Intent for one of the two screens.
    static Intent wordCountIntent = new Intent();
    private String originalText = "";
    private TextView resultView;

    private final ActivityResultLauncher<Intent> textInputLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    originalText = result.getData().getStringExtra(Intent.EXTRA_TEXT);
                    resultView.setText(originalText);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        resultView = findViewById(R.id.tvResult);

        if (savedInstanceState != null) {
            originalText = savedInstanceState.getString("originalText", "");
            resultView.setText(savedInstanceState.getString("displayedText"));
        } else if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            CharSequence sharedText = getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                resultView.setText(sharedText);
            }
        }

        findViewById(R.id.btnEnterText).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TextInputActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, originalText);
            textInputLauncher.launch(intent);
        });
        findViewById(R.id.btnCountWords).setOnClickListener(v -> {
            wordCountIntent = new Intent(MainActivity.this, WordCountActivity.class);
            wordCountIntent.putExtra(Intent.EXTRA_TEXT, originalText);
            startActivity(wordCountIntent);
        });
        findViewById(R.id.btnShareText).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, resultView.getText().toString());
            startActivity(Intent.createChooser(intent, null));
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String countedText = wordCountIntent.getStringExtra("countedText");
        if (countedText != null) {
            resultView.setText(countedText);
            wordCountIntent.removeExtra("countedText");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("originalText", originalText);
        outState.putString("displayedText", resultView.getText().toString());
    }
}
