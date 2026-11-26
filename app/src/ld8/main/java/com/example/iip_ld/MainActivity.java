package com.example.iip_ld;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ToneGenerator tones; // plays short beep sounds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The phone's volume buttons will control the "music/media" volume in this app.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // Create the beep player at 80% volume.
        tones = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        // Use our custom view as the whole screen (instead of R.layout.activity_main).
        setContentView(new ShapeView(this, tones));
    }

    @Override
    protected void onDestroy() {
        // Free the sound resources when the screen is closed.
        tones.release();
        super.onDestroy();
    }
}
