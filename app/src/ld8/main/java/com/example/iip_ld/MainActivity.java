package com.example.iip_ld;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ToneGenerator tones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        tones = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        setContentView(new ShapeView(this, tones));
    }

    @Override
    protected void onDestroy() {
        tones.release();
        super.onDestroy();
    }
}
