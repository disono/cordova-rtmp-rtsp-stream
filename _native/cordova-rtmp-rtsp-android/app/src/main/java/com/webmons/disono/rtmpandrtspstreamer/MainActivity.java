package com.webmons.disono.rtmpandrtspstreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Wowza Configuration
 *
 * Config
 * http://your-ip/enginemanager
 * https://www.wowza.com/docs/how-to-set-up-live-streaming-using-an-rtmp-based-encoder
 * https://www.wowza.com/docs/how-to-set-up-live-streaming-using-a-native-rtp-encoder-with-sdp-file
 *
 * Server URL: rtmp://[wowza-ip-address]/live
 * Stream Name: myStream
 * User: publisherName
 * password: [password]
 *
 * Verify your server or endpoint using ffmpeg
 * ffmpeg -i pathtomp4file -f flv rtmp://yourendpoint
 */

public class MainActivity extends AppCompatActivity {
    public final String _USERNAME = "";
    public final String _PASSWORD = "";

    public final String _URL_RTSP = "";
    public final String _URL_RTMP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _startRTMP();
    }

    private void _startRTSP()
    {
        Intent intent = new Intent(this, RTSPActivity.class);
        intent.putExtra("username", _USERNAME);
        intent.putExtra("password", _PASSWORD);
        intent.putExtra("url", _URL_RTSP);
        startActivity(intent);
    }

    private void _startRTMP()
    {
        Intent intent = new Intent(this, RTMPActivity.class);
        intent.putExtra("username", _USERNAME);
        intent.putExtra("password", _PASSWORD);
        intent.putExtra("url", _URL_RTMP);
        startActivity(intent);
    }
}
