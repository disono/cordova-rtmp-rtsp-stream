package com.webmons.disono.rtmpandrtspstreamer;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.pedro.encoder.input.video.Camera1ApiManager;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtsp.rtsp.Protocol;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

import org.apache.cordova.CordovaActivity;

import io.cordova.hellocordova.R;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: http://www.webmons.com
 *
 * Created at: 1/09/2018
 */

public class RTSPActivity extends CordovaActivity implements ConnectCheckerRtsp {
    SurfaceView surfaceView;
    private RtspCamera1 rtspCameral;
    private Activity activity;
	
    private String _url = null;
    private String _username = null;
    private String _password = null;

    private ImageButton ic_torch;
    private ImageButton ic_switch_camera;
    private ImageButton ic_broadcast;
    private Camera1ApiManager camera1ApiManager;
    private boolean isFlashOn = false;
    private boolean isStreamingOn = false;
	
	BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String method = intent.getStringExtra("method");

                if (method != null) {
                    switch (method) {
                        case "stop":
                            _stopStreaming();
                            break;
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.cordovaInterface.getActivity();
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        _username = intent.getStringExtra("username");
        _password = intent.getStringExtra("password");
        _url = intent.getStringExtra("url");
        
        _UIListener();
		_broadcastRCV();
    }

    @Override
    public void onPause() {
        super.onPause();
        _stopStreaming();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _stopStreaming();
    }

    @Override
    public void onConnectionSuccessRtsp() {
        runOnUiThread(() -> Toast.makeText(RTSPActivity.this, "Connection success", Toast.LENGTH_SHORT)
                .show());
    }

    @Override
    public void onConnectionFailedRtsp(final String reason) {
        runOnUiThread(() -> {
            Toast.makeText(RTSPActivity.this, "Connection failed. " + reason,
                    Toast.LENGTH_SHORT).show();
            _stopStreaming();
        });
    }

    @Override
    public void onDisconnectRtsp() {
        runOnUiThread(() -> Toast.makeText(RTSPActivity.this, "Disconnected", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAuthErrorRtsp() {
        runOnUiThread(() -> Toast.makeText(RTSPActivity.this, "Auth error", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAuthSuccessRtsp() {
        runOnUiThread(() -> Toast.makeText(RTSPActivity.this, "Auth success", Toast.LENGTH_SHORT).show());
    }
	
	private void _broadcastRCV() {
        IntentFilter filter = new IntentFilter("com.webmons.disono.rtmpandrtspstreamer");
        activity.registerReceiver(br, filter);
    }

    private void _UIListener() {
        surfaceView = findViewById(R.id.surfaceView);
        rtspCameral = new RtspCamera1(surfaceView, this);
        camera1ApiManager = new Camera1ApiManager(surfaceView, rtspCameral);

        ic_torch = findViewById(R.id.ic_torch);
        ic_torch.setOnClickListener(v -> _toggleFlash());

        ic_switch_camera = findViewById(R.id.ic_switch_camera);
        ic_switch_camera.setOnClickListener(v -> _toggleCameraFace());

        ic_broadcast = findViewById(R.id.ic_broadcast);
        ic_broadcast.setOnClickListener(v -> _toggleStreaming());
    }

    private void _toggleStreaming() {
        if (!rtspCameral.isStreaming()) {
            _startStreaming();
        } else {
            _stopStreaming();
        }

        _toggleBtnImgVideo();
    }

    private void _startStreaming() {
        rtspCameral.setProtocol(Protocol.TCP);
        rtspCameral.setAuthorization(_username, _password);

        if (rtspCameral.prepareAudio() && rtspCameral.prepareVideo()) {
            rtspCameral.startStream(_url);
            isStreamingOn = true;
        } else {
            Toast.makeText(activity, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT)
                    .show();

            isStreamingOn = false;
        }
    }

    private void _stopStreaming() {
        if (rtspCameral.isStreaming()) {
            _toggleFlash();
            isStreamingOn = false;

            rtspCameral.stopStream();
            rtspCameral.stopPreview();
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
            rtspCameral.switchCamera();
        } catch (CameraOpenException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            rtspCameral.switchCamera();
        }
    }
}
