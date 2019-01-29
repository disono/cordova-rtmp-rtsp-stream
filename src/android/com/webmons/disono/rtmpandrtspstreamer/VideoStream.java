package com.webmons.disono.rtmpandrtspstreamer;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: https://webmons.com
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
    public final static String BROADCAST_LISTENER = "com.webmons.disono.rtmpandrtspstreamer.results";
    public final static String BROADCAST_FILTER = "com.webmons.disono.rtmpandrtspstreamer";
    private final static String TAG = "VideoStream";
    private static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final String CAMERA = Manifest.permission.CAMERA;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String MODIFY_AUDIO_SETTINGS = Manifest.permission.MODIFY_AUDIO_SETTINGS;
    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WAKE_LOCK = Manifest.permission.WAKE_LOCK;
    private static final int REQ_CODE = 500;
    private static final String PERMISSION_DENIED_ERROR = "Permissions denied.";
    private CallbackContext callbackContext;
    private JSONArray _args = null;

    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String method = intent.getStringExtra("method");
                String data = intent.getStringExtra("data");
                Log.d(TAG, "Method: " + method + " Data: " + data);

                if (method != null) {
                    switch (method) {
                        case "onConnectionSuccess":
                            _cordovaSendResult("onConnectionSuccess", data);

                            break;
                        case "onConnectionFailed":
                            _plugResultError("Connection failed.");

                            break;
                        case "onDisconnect":
                            _plugResultError("Disconnected from the stream server.");

                            break;
                        case "onAuthError":
                            _plugResultError("Authentication error, invalid credentials.");

                            break;
                        case "onAuthSuccess":
                            _cordovaSendResult("onAuthSuccess", data);

                            break;
                        case "onStartStream":
                            _cordovaSendResult("onStartStream", data);

                            break;

                        case "onStopStream":
                            _cordovaSendResult("onStopStream", data);

                            break;

                        case "onError":
                            if (data != null) {
                                try {
                                    JSONObject obj = new JSONObject(data);
                                    _plugResultError(obj.getString("message"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    _plugResultError(e.getMessage());
                                }
                            } else {
                                _plugResultError("Unknown error occurred.");
                            }

                            break;
                        case "onCommentSend":
                            _cordovaSendResult("onCommentSend", data);

                            break;
                        case "onCommentItemSelected":
                            _cordovaSendResult("onCommentItemSelected", data);

                            break;
                    }
                }
            }
        }
    };
    private Activity mActivity;
    private String[] permissions = {WRITE_EXTERNAL_STORAGE, CAMERA, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS,
            READ_EXTERNAL_STORAGE, WAKE_LOCK};
    private String url;
    private String username;
    private String password;
    private String _action;

    static void sendBroadCast(Activity activity, String methodName) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_LISTENER);
        intent.putExtra("method", methodName);
        activity.sendBroadcast(intent);
    }

    public static void sendBroadCast(Activity activity, String methodName, JSONObject object) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_LISTENER);
        intent.putExtra("method", methodName);
        intent.putExtra("data", object.toString());
        activity.sendBroadcast(intent);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // application context
        mActivity = cordova.getActivity();
        _action = action;
        if (this.callbackContext == null) {
            this.callbackContext = callbackContext;
        }

        _args = args;
        url = args.getString(0);
        if (args.length() == 3) {
            username = args.getString(1);
            password = args.getString(2);
        }

        return this._methods(action, _args);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        int i = 0;
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR +
                        " Request Code: " + REQ_CODE + ", Actions: " + _action + ", Permission: " + permissions[i]));
                return;
            }
            i++;
        }

        if (requestCode == REQ_CODE) {
            this._methods(_action, _args);
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
        mActivity.unregisterReceiver(br);
        _filters("stop");
    }

    private boolean _methods(String action, JSONArray args) throws JSONException {
        if (cordova.hasPermission(WRITE_EXTERNAL_STORAGE) && cordova.hasPermission(CAMERA) &&
                cordova.hasPermission(RECORD_AUDIO) && cordova.hasPermission(MODIFY_AUDIO_SETTINGS) &&
                cordova.hasPermission(READ_EXTERNAL_STORAGE)) {
            switch (action) {
                case "streamRTSP":
                    _startRTSP(url, null, null);
                    _plugResultsKeep();

                    return true;
                case "streamRTSPAuth":
                    _startRTSP(url, username, password);
                    _plugResultsKeep();

                    return true;
                case "streamRTMP":
                    _startRTMP(url, null, null);
                    _plugResultsKeep();

                    return true;
                case "streamRTMPAuth":
                    _startRTMP(url, username, password);
                    _plugResultsKeep();

                    return true;
                case "streamStop":
                    _filters("stop");
                    _plugResultsKeep();

                    return true;
                case "commentList":
                    _filters("commentList", args.getJSONArray(0));
                    _plugResultsKeep();

                    return true;
                case "commentListShow":
                    _filters("commentListShow", args.getBoolean(0));
                    _plugResultsKeep();

                    return true;
                case "videoRecord":
                    _filters("videoRecord");
                    _plugResultsKeep();

                    return true;
            }
        } else {
            _getReadPermission(REQ_CODE);
            _plugResultsKeep();

            return true;
        }

        _plugResultsKeep();
        return false;
    }

    private void _getReadPermission(int requestCode) {
        cordova.requestPermissions(this, requestCode, permissions);
    }

    private void _startRTSP(String uri, String username, String password) {
        _broadcastRCV();

        Intent intent = new Intent(mActivity, RTSPActivity.class);
        intent.putExtra("url", uri);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        mActivity.startActivity(intent);
    }

    private void _startRTMP(String uri, String username, String password) {
        _broadcastRCV();

        Intent intent = new Intent(mActivity, RTMPActivity.class);
        intent.putExtra("url", uri);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        mActivity.startActivity(intent);
    }

    private void _filters(String methodName) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_FILTER);
        intent.putExtra("method", methodName);
        mActivity.sendBroadcast(intent);
    }

    private void _filters(String methodName, JSONArray objects) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_FILTER);
        intent.putExtra("method", methodName);
        intent.putExtra("data", objects.toString());
        mActivity.sendBroadcast(intent);
    }

    private void _filters(String methodName, boolean option) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_FILTER);
        intent.putExtra("method", methodName);
        intent.putExtra("option", option);
        mActivity.sendBroadcast(intent);
    }

    private void _plugResultsKeep() {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    private void _plugResultError(String message) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, message);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    private void _cordovaSendResult(String event, String data) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("event_name", event);
            obj.put("data", (data != null) ? new JSONObject(data) : "");
        } catch (JSONException e) {
            e.printStackTrace();
            _plugResultError(e.getMessage());
            return;
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    private void _broadcastRCV() {
        IntentFilter filter = new IntentFilter(BROADCAST_LISTENER);
        mActivity.registerReceiver(br, filter);
    }

    public void scanFiles(Context ctx, File file) {
        new SingleMediaScanner(ctx, file);
    }
}
