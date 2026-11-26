package com.example.iip_ld;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
public class MainActivity extends AppCompatActivity {
    private MapView map;    // the map on the screen
    private Marker marker;  // the pin; null until the first coordinates are entered

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OpenStreetMap asks every app to identify itself (the "user agent").
        Configuration.getInstance().setUserAgentValue("IIP-LD9/1.0 (" + getPackageName() + ")");
        // Store downloaded map tiles in the app's private folder (a cache, so they load faster).
        Configuration.getInstance().setOsmdroidTileCache(getDir("osm_tiles", MODE_PRIVATE));
        // Load the look of the screen from res/layout/activity_main.xml.
        setContentView(R.layout.activity_main);

        // Leave room for the system bars and the keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        map = findViewById(R.id.map);
        // Where to download map pictures from: zoom levels 0-19, 256x256 px PNG images.
        map.setTileSource(new XYTileSource("OpenStreetMap", 0, 19, 256, ".png",
                new String[]{"https://tile.openstreetmap.org/"}));
        // Allow pinch-to-zoom with two fingers.
        map.setMultiTouchControls(true);
        // Start zoomed out a bit, centred on Kaunas (latitude 54.8985, longitude 23.9036).
        map.getController().setZoom(8.0);
        map.getController().setCenter(new GeoPoint(54.8985, 23.9036));

        EditText longitude = findViewById(R.id.longitude);
        EditText latitude = findViewById(R.id.latitude);
        // "Įvesti koordinates" button: show the typed coordinates on the map.
        findViewById(R.id.show_coordinates).setOnClickListener(
                view -> showCoordinates(longitude, latitude));
    }

    // Checks the typed numbers, then places the marker and moves the map there.
    private void showCoordinates(EditText longitude, EditText latitude) {
        // Longitude must be a number from -180 to 180.
        double lon = readCoordinate(longitude);
        if (!Double.isFinite(lon) || Math.abs(lon) > 180) {
            // setError shows a red "!" with a message next to the text box.
            longitude.setError(getString(R.string.invalid_longitude));
            return;
        }
        // Latitude must be from -85.0511 to 85.0511 (the furthest the flat map can show).
        double lat = readCoordinate(latitude);
        if (!Double.isFinite(lat) || Math.abs(lat) > 85.0511) {
            latitude.setError(getString(R.string.invalid_latitude));
            return;
        }

        // The form starts with longitude, but GeoPoint expects latitude first.
        GeoPoint point = new GeoPoint(lat, lon);
        // Create the marker only the first time; after that we just move it.
        if (marker == null) {
            marker = new Marker(map);
            // The bottom tip of the pin points at the exact location.
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            // "Overlays" are things drawn on top of the map.
            map.getOverlays().add(marker);
        }
        marker.setPosition(point);
        // Text shown when the marker is tapped.
        marker.setTitle(lat + ", " + lon);
        // Move the map so the point is in the middle, and redraw.
        map.getController().setCenter(point);
        map.invalidate();

        // Hide the on-screen keyboard so the map is visible.
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(latitude.getWindowToken(), 0);
    }

    // Turns the text in a box into a number. Returns NaN ("not a number") if it can't.
    private double readCoordinate(EditText field) {
        try {
            // Accept "54,9" as well as "54.9", and the special minus sign "−" as "-".
            String value = field.getText().toString().trim().replace(',', '.').replace('−', '-');
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    // The map must be told when the screen is paused/resumed (osmdroid requirement).
    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        map.onPause();
        super.onPause();
    }
}
