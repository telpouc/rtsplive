package com.telpo.rtspstreaming;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * MainActivity
 * @author gfm
 */
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 1;
    private static final String[] PERMISSIONS = { Manifest.permission.CAMERA };
    private MediaStream mediaStream;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvRtspUrl = findViewById(R.id.tvRtspUrl);
        surfaceView = findViewById(R.id.surfaceView);
        mediaStream = new MediaStream();
        surfaceView.getHolder().addCallback(mediaStream);
        mediaStream.getRtspUrl().observe(this, tvRtspUrl::setText);
    }

    @Override
    protected void onDestroy() {
        surfaceView.getHolder().removeCallback(mediaStream);
        mediaStream.stopRtspServer();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST);
    }

    @Override
    protected void onStop() {
        mediaStream.stopPreview();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                mediaStream.startPreview();
            }
        }
    }
}
