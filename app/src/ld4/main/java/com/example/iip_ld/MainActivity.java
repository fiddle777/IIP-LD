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
    // Text that gets filled in on double tap.
    private static final String ADDRESS_PREFIX = "https://www";
    // The "key" (name) under which the last address is saved.
    private static final String LAST_URL = "lastUrl";

    private EditText address;   // the address box at the top
    private WebView browser;    // the area that shows the web page
    private boolean warnedAboutExit; // true if we already showed the "next Back closes" warning

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        // Load the look of the screen from res/layout/activity_main.xml.
        setContentView(R.layout.activity_main);

        // Leave room for the system bars and the keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        // Make the status bar icons light, so they are visible on the purple background.
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);

        address = findViewById(R.id.address);
        browser = findViewById(R.id.browser);
        // SharedPreferences = a small storage of "key -> value" pairs that survives app restarts.
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        // Keep website links inside this WebView. Some sites need JavaScript to render.
        browser.getSettings().setJavaScriptEnabled(true);
        // Setting a WebViewClient means clicked links open HERE, not in Chrome.
        browser.setWebViewClient(new WebViewClient() {
            // Called when a new page becomes visible on the screen.
            @Override
            public void onPageCommitVisible(WebView view, String url) {
                // Save the page when it becomes visible, including after using Back.
                // The local instructions page has no website address to save.
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    // Show the real address in the box and move the cursor to the end.
                    address.setText(url);
                    address.setSelection(address.length());
                    // Remember this address for the next time the app starts.
                    preferences.edit().putString(LAST_URL, url).apply();
                }
            }
        });

        // Copy the hint into the empty field on a double tap so it can be edited.
        // GestureDetector is a helper that recognises gestures like double taps.
        GestureDetector doubleTap = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTapEvent(MotionEvent event) {
                        // Only when the finger is lifted after the 2nd tap, and the box is empty.
                        if (event.getActionMasked() == MotionEvent.ACTION_UP && address.length() == 0) {
                            address.post(() -> {
                                address.setText(ADDRESS_PREFIX);
                                address.setSelection(address.length());
                            });
                        }
                        return false;
                    }
                });
        // Pass every touch on the address box to the gesture detector.
        // Returning false lets the box still work normally (cursor, keyboard...).
        address.setOnTouchListener((view, event) -> {
            doubleTap.onTouchEvent(event);
            return false;
        });

        // "Užkrauti tinklapį" button: open the typed address.
        findViewById(R.id.load_page).setOnClickListener(view -> {
            String url = address.getText().toString().trim();
            // Nothing useful typed - do nothing.
            if (url.isEmpty() || url.equals(ADDRESS_PREFIX)) {
                return;
            }

            // Let the user enter either a full link or just the site name.
            // e.g. "google.com" becomes "https://google.com".
            if (!url.contains("://")) {
                url = "https://" + url;
            }
            // Only allow normal web addresses.
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                return;
            }

            // Hide the on-screen keyboard so the page is visible.
            getSystemService(InputMethodManager.class)
                    .hideSoftInputFromWindow(address.getWindowToken(), 0);
            warnedAboutExit = false;
            // Open the page.
            browser.loadUrl(url);
        });

        // Show a welcome page only when there is no previously viewed website.
        String lastUrl = preferences.getString(LAST_URL, "");
        if (lastUrl.isEmpty()) {
            // First start: show a small HTML page with instructions (text from strings.xml).
            browser.loadDataWithBaseURL(null,
                    "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                            + "<body style='font-family:sans-serif'>"
                            + getString(R.string.browser_instructions) + "</body>",
                    "text/html", "UTF-8", null);
        } else {
            // Not the first start: open the page the user saw last time.
            address.setText(lastUrl);
            address.setSelection(address.length());
            browser.loadUrl(lastUrl);
        }

        // Custom behaviour for the phone's Back button.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back goes through pages first. At the start, warn before closing.
                if (browser.canGoBack()) {
                    // Index 1 = we are on the 2nd page, so going back lands on the first one.
                    boolean returningToFirstPage = browser.copyBackForwardList().getCurrentIndex() == 1;
                    browser.goBack();
                    warnedAboutExit = false;
                    if (returningToFirstPage) {
                        warnAboutExit();
                    }
                } else if (warnedAboutExit) {
                    // Already warned once - now really close the app.
                    finish();
                } else {
                    warnAboutExit();
                }
            }
        });
    }

    // Shows "next Back will close the browser" and remembers that we warned.
    private void warnAboutExit() {
        warnedAboutExit = true;
        Toast.makeText(this, R.string.back_warning, Toast.LENGTH_LONG).show();
    }
}
