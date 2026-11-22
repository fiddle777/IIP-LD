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
    private MapView map;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue("IIP-LD9/1.0 (" + getPackageName() + ")");
        Configuration.getInstance().setOsmdroidTileCache(getDir("osm_tiles", MODE_PRIVATE));
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        map = findViewById(R.id.map);
        map.setTileSource(new XYTileSource("OpenStreetMap", 0, 19, 256, ".png",
                new String[]{"https://tile.openstreetmap.org/"}));
        map.setMultiTouchControls(true);
        map.getController().setZoom(8.0);
        map.getController().setCenter(new GeoPoint(54.8985, 23.9036));

        EditText longitude = findViewById(R.id.longitude);
        EditText latitude = findViewById(R.id.latitude);
        findViewById(R.id.show_coordinates).setOnClickListener(
                view -> showCoordinates(longitude, latitude));
    }

    private void showCoordinates(EditText longitude, EditText latitude) {
        double lon = readCoordinate(longitude);
        if (!Double.isFinite(lon) || Math.abs(lon) > 180) {
            longitude.setError(getString(R.string.invalid_longitude));
            return;
        }
        double lat = readCoordinate(latitude);
        if (!Double.isFinite(lat) || Math.abs(lat) > 85.0511) {
            latitude.setError(getString(R.string.invalid_latitude));
            return;
        }

        // The form starts with longitude, but GeoPoint expects latitude first.
        GeoPoint point = new GeoPoint(lat, lon);
        if (marker == null) {
            marker = new Marker(map);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(marker);
        }
        marker.setPosition(point);
        marker.setTitle(lat + ", " + lon);
        map.getController().setCenter(point);
        map.invalidate();

        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(latitude.getWindowToken(), 0);
    }

    private double readCoordinate(EditText field) {
        try {
            String value = field.getText().toString().trim().replace(',', '.').replace('−', '-');
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

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
