package com.webmons.disono.rtmpandrtspstreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.pedro.encoder.input.video.Camera1ApiManager;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import net.ossrs.rtmp.ConnectCheckerRtmp;

public class RTMPActivity extends AppCompatActivity implements ConnectCheckerRtmp {
    private static String TAG = "RTMPActivity";
    SurfaceView surfaceView;
    private RtmpCamera1 rtmpCameral;

    private String _url = null;
    private String _username = null;
    private String _password = null;

    private ImageButton ic_torch;
    private ImageButton ic_switch_camera;
    private ImageButton ic_broadcast;
    private Camera1ApiManager camera1ApiManager;
    private boolean isFlashOn = false;
    private boolean isStreamingOn = false;

    PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |
                    PowerManager.ON_AFTER_RELEASE, TAG);
            mWakeLock.acquire(10);
        }

        Intent intent = getIntent();
        _username = intent.getStringExtra("username");
        _password = intent.getStringExtra("password");
        _url = intent.getStringExtra("url");

        _UIListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _stopStreaming();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _stopStreaming();

        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RTMPActivity.this, "Connection success", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RTMPActivity.this, "Connection failed. " + reason,
                        Toast.LENGTH_SHORT).show();
                _stopStreaming();
            }
        });
    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RTMPActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RTMPActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RTMPActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void _UIListener() {
        surfaceView = findViewById(R.id.surfaceView);
        rtmpCameral = new RtmpCamera1(surfaceView, this);
        camera1ApiManager = new Camera1ApiManager(surfaceView, rtmpCameral);

        ic_torch = findViewById(R.id.ic_torch);
        ic_torch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _toggleFlash();
            }
        });

        ic_switch_camera = findViewById(R.id.ic_switch_camera);
        ic_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _toggleCameraFace();
            }
        });

        ic_broadcast = findViewById(R.id.ic_broadcast);
        ic_broadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _toggleStreaming();
            }
        });
    }

    private void _toggleStreaming() {
        if (!rtmpCameral.isStreaming()) {
            _startStreaming();
        } else {
            _stopStreaming();
        }

        _toggleBtnImgVideo();
    }

    private void _startStreaming() {
        rtmpCameral.setAuthorization(_username, _password);

        if (rtmpCameral.prepareAudio() && rtmpCameral.prepareVideo()) {
            rtmpCameral.startStream(_url);
            isStreamingOn = true;
        } else {
            Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT)
                    .show();

            isStreamingOn = false;
        }
    }

    private void _stopStreaming() {
        if (rtmpCameral.isStreaming()) {
            _toggleFlash();
            isStreamingOn = false;

            rtmpCameral.stopStream();
            rtmpCameral.stopPreview();
        }
    }

    private void _toggleFlash() {
        if (!isFlashOn) {
            camera1ApiManager.enableLantern();
            isFlashOn = true;
        } else {
            camera1ApiManager.disableLantern();
            isFlashOn = false;
        }

        // changing button/switch image
        _toggleBtnImgFlash();
    }

    private void _toggleBtnImgFlash() {
        String icon = (!isFlashOn) ? "ic_flash_off_white_48dp" : "ic_flash_on_white_48dp";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ic_torch.setImageDrawable(getDrawable(getResources().getIdentifier(icon, "drawable", getPackageName())));
        } else {
            ic_torch.setImageResource(getResources().getIdentifier(icon, "drawable", getPackageName()));
        }
    }

    private void _toggleBtnImgVideo() {
        String icon = (!isStreamingOn) ? "ic_videocam_white_48dp" : "ic_videocam_off_white_48dp";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ic_broadcast.setImageDrawable(getDrawable(getResources().getIdentifier(icon, "drawable", getPackageName())));
        } else {
            ic_broadcast.setImageResource(getResources().getIdentifier(icon, "drawable", getPackageName()));
        }
    }

    private void _toggleCameraFace() {
        try {
            rtmpCameral.switchCamera();
        } catch (CameraOpenException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            rtmpCameral.switchCamera();
        }
    }
}
