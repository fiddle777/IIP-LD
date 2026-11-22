package com.example.iip_ld;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final String ADDRESS_PREFIX = "https://www";
    private static final String LAST_URL = "lastUrl";

    private EditText address;
    private WebView browser;
    private boolean warnedAboutExit;

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // Leave room for the system bars and the keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);

        address = findViewById(R.id.address);
        browser = findViewById(R.id.browser);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        // Keep website links inside this WebView. Some sites need JavaScript to render.
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageCommitVisible(WebView view, String url) {
                // Save the page when it becomes visible, including after using Back.
                // The local instructions page has no website address to save.
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    address.setText(url);
                    address.setSelection(address.length());
                    preferences.edit().putString(LAST_URL, url).apply();
                }
            }
        });

        // Copy the hint into the empty field on a double tap so it can be edited.
        GestureDetector doubleTap = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTapEvent(MotionEvent event) {
                        if (event.getActionMasked() == MotionEvent.ACTION_UP && address.length() == 0) {
                            address.post(() -> {
                                address.setText(ADDRESS_PREFIX);
                                address.setSelection(address.length());
                            });
                        }
                        return false;
                    }
                });
        address.setOnTouchListener((view, event) -> {
            doubleTap.onTouchEvent(event);
            return false;
        });

        findViewById(R.id.load_page).setOnClickListener(view -> {
            String url = address.getText().toString().trim();
            if (url.isEmpty() || url.equals(ADDRESS_PREFIX)) {
                return;
            }

            // Let the user enter either a full link or just the site name.
            if (!url.contains("://")) {
                url = "https://" + url;
            }
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                return;
            }

            getSystemService(InputMethodManager.class)
                    .hideSoftInputFromWindow(address.getWindowToken(), 0);
            warnedAboutExit = false;
            browser.loadUrl(url);
        });

        // Show a welcome page only when there is no previously viewed website.
        String lastUrl = preferences.getString(LAST_URL, "");
        if (lastUrl.isEmpty()) {
            browser.loadDataWithBaseURL(null,
                    "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                            + "<body style='font-family:sans-serif'>"
                            + getString(R.string.browser_instructions) + "</body>",
                    "text/html", "UTF-8", null);
        } else {
            address.setText(lastUrl);
            address.setSelection(address.length());
            browser.loadUrl(lastUrl);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back goes through pages first. At the start, warn before closing.
                if (browser.canGoBack()) {
                    boolean returningToFirstPage = browser.copyBackForwardList().getCurrentIndex() == 1;
                    browser.goBack();
                    warnedAboutExit = false;
                    if (returningToFirstPage) {
                        warnAboutExit();
                    }
                } else if (warnedAboutExit) {
                    finish();
                } else {
                    warnAboutExit();
                }
            }
        });
    }

    private void warnAboutExit() {
        warnedAboutExit = true;
        Toast.makeText(this, R.string.back_warning, Toast.LENGTH_LONG).show();
    }
}
