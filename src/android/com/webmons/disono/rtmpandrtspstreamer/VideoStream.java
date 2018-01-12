package com.webmons.disono.rtmpandrtspstreamer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: http://www.webmons.com
 *
 * Created at: 1/09/2018
 */

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

public class VideoStream extends CordovaPlugin {
    private CallbackContext callbackContext;
    PluginResult pluginResult;
    private Activity activity;

    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String CAMERA = Manifest.permission.CAMERA;
    private static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private String[] permissions = {WRITE_EXTERNAL_STORAGE, CAMERA, RECORD_AUDIO};
    private static final int REQ_CODE = 500;
    private static final String PERMISSION_DENIED_ERROR = "Permissions denied.";

    private String url;
    private String username;
    private String password;
    private String _action;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // application context
        activity = cordova.getActivity();
        this.callbackContext = callbackContext;
        _action = action;

        url = args.getString(0);
        if (args.length() == 3) {
            username = args.getString(1);
            password = args.getString(2);
        }

        if (cordova.hasPermission(WRITE_EXTERNAL_STORAGE) && cordova.hasPermission(CAMERA) && cordova.hasPermission(RECORD_AUDIO)) {
            switch (action) {
                case "streamRTSP":
                    _startRTSP(url, null, null);

                    // Don't return any result now
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);

                    return true;
                case "streamRTSPAuth":
                    _startRTSP(url, username, password);

                    // Don't return any result now
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);

                    return true;
                case "streamRTMP":
                    _startRTMP(url, null, null);

                    // Don't return any result now
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);

                    return true;
                case "streamRTMPAuth":
                    _startRTMP(url, username, password);

                    // Don't return any result now
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);

                    return true;
                case "streamStop":
                    _filters("stop");

                    return true;
            }
        } else {
            _getReadPermission(REQ_CODE);
        }

        return false;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }

        if (requestCode == REQ_CODE) {
            switch (_action) {
                case "streamRTSP":
                    _startRTSP(url, null, null);

                    // Don't return any result now
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);

                    break;
                case "streamRTSPAuth":
                    _startRTSP(url, username, password);

                    // Don't return any result now
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);

                    break;
                case "streamRTMP":
                    _startRTMP(url, null, null);

                    // Don't return any result now
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);

                    break;
                case "streamRTMPAuth":
                    _startRTMP(url, username, password);

                    // Don't return any result now
                    pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);

                    break;
                case "streamStop":
                    _filters("stop");

                    break;
            }
        }
    }

    @Override
    public void onPause(boolean p) {
        super.onPause(p);
        _filters("stop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _filters("stop");
    }

    private void _getReadPermission(int requestCode) {
        cordova.requestPermissions(this, requestCode, permissions);
    }

    private void _startRTSP(String uri, String username, String password) {
        Intent intent = new Intent(activity, RTSPActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("url", uri);
        activity.startActivity(intent);
    }

    private void _startRTMP(String uri, String username, String password) {
        Intent intent = new Intent(activity, RTMPActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("url", uri);
        activity.startActivity(intent);
    }

    private void _filters(String methodName) {
        Intent intent = new Intent();
        intent.setAction("com.webmons.disono.rtmpandrtspstreamer");
        intent.putExtra("method", methodName);
        activity.sendBroadcast(intent);
    }
}
