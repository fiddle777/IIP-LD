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
    private EditText textInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_input);
        textInput = findViewById(R.id.etInputText);
        textInput.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
        findViewById(R.id.btnConfirmText).setOnClickListener(v -> returnText());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnText();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void returnText() {
        Intent result = new Intent();
        result.putExtra(Intent.EXTRA_TEXT, textInput.getText().toString());
        setResult(RESULT_OK, result);
        finish();
    }
}
