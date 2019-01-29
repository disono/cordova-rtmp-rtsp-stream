package com.webmons.disono.rtmpandrtspstreamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.encoder.input.video.Camera1ApiManager;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import org.apache.cordova.CordovaActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: https://webmons.com
 * 
 * Created at: 1/09/2018
 */

public class RTMPActivity extends CordovaActivity implements ConnectCheckerRtmp {
    // 90, 180, 270 or 0
    private final String[] _orient = new String[]{"90", "180", "270", "0"};
    SurfaceView surfaceView;
    String currentDateAndTime;
    PowerManager.WakeLock mWakeLock;
    private RtmpCamera1 rtmpCameral;
    private Activity activity;
    private File folder = null;
    private String _url = null;
    private String _username = null;
    private String _password = null;
    private ImageButton ic_torch;
    private ImageButton ic_resolutions;
    private ImageButton ic_switch_camera;
    private ImageButton ic_preview_orientation;
    private ImageButton ic_broadcast;
    private ImageButton ic_record;
    private ImageButton ic_closed;
    private Camera1ApiManager camera1ApiManager;
    private boolean isFlashOn = false;

    // comment
    private LinearLayout commentForm;
    private CommentListAdapter adapter;
    private ArrayList<Comments> mComments = null;
    private ListView list;
    private EditText txtComment;
    private ImageButton btnComment;

    private java.util.List<android.hardware.Camera.Size> resolutions;
    private int selectedWidth = 0;
    private int selectedHeight = 0;
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
                        case "commentList":
                            _commentList(intent.getStringExtra("data"));
                            break;
                        case "commentListShow":
                            _commentFormVisible(intent.getBooleanExtra("option", false));
                            break;
                        case "videoRecord":
                            _toggleRecording();
                            break;
                    }
                }
            }
        }
    };
    private boolean isListShow = false;

    private File _createFolder() {
        if (Environment.getExternalStorageState() == null) {
            return new File(Environment.getDataDirectory().getAbsolutePath()
                    + "/video-recordings");
        } else if (Environment.getExternalStorageState() != null) {
            return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/video-recordings");
        }

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.cordovaInterface.getActivity();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(_getResource("rtsp_rtmp_streamer", "layout"));

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |
                    PowerManager.ON_AFTER_RELEASE, TAG);
            mWakeLock.acquire();
        }

        folder = _createFolder();

        Intent intent = getIntent();
        _url = intent.getStringExtra("url");
        _username = intent.getStringExtra("username");
        _password = intent.getStringExtra("password");

        _UIListener();
        _commentContainer(false);
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
        activity.unregisterReceiver(br);
        _stopStreaming();

        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @Override
    public void onConnectionSuccessRtmp() {
        VideoStream.sendBroadCast(activity, "onConnectionSuccess");
        runOnUiThread(() -> Toast.makeText(RTMPActivity.this, "Connection success", Toast.LENGTH_SHORT)
                .show());
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        VideoStream.sendBroadCast(activity, "onConnectionFailed");
        runOnUiThread(() -> {
            Toast.makeText(RTMPActivity.this, "Connection failed. " + reason,
                    Toast.LENGTH_SHORT).show();
            _stopStreaming();
        });
    }

    @Override
    public void onDisconnectRtmp() {
        VideoStream.sendBroadCast(activity, "onDisconnect");
        runOnUiThread(() -> {
            Toast.makeText(RTMPActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            _stopStreaming();
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        VideoStream.sendBroadCast(activity, "onAuthError");
        runOnUiThread(() -> {
            Toast.makeText(RTMPActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
            _stopStreaming();
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        VideoStream.sendBroadCast(activity, "onAuthSuccess");
        runOnUiThread(() -> Toast.makeText(RTMPActivity.this, "Auth success", Toast.LENGTH_SHORT).show());
    }

    private void _broadcastRCV() {
        IntentFilter filter = new IntentFilter(VideoStream.BROADCAST_FILTER);
        activity.registerReceiver(br, filter);
    }

    private void _UIListener() {
        surfaceView = findViewById(_getResource("rtmp_rtsp_stream_surfaceView", "id"));
        rtmpCameral = new RtmpCamera1(surfaceView, this);
        camera1ApiManager = new Camera1ApiManager(surfaceView, rtmpCameral);

        ic_torch = findViewById(_getResource("ic_torch", "id"));
        ic_torch.setOnClickListener(v -> _toggleFlash());

        ic_resolutions = findViewById(_getResource("ic_resolutions", "id"));
        ic_resolutions.setOnClickListener(v -> _settingsDialog());

        ic_switch_camera = findViewById(_getResource("ic_switch_camera", "id"));
        ic_switch_camera.setOnClickListener(v -> _toggleCameraFace());

        ic_preview_orientation = findViewById(_getResource("ic_preview_orientation", "id"));
        ic_preview_orientation.setOnClickListener(v -> _changeOrientation());

        ic_broadcast = findViewById(_getResource("ic_broadcast", "id"));
        ic_broadcast.setOnClickListener(v -> _toggleStreaming());

        ic_record = findViewById(_getResource("ic_record", "id"));
        ic_record.setOnClickListener(v -> _toggleRecording());

        ic_closed = findViewById(_getResource("ic_closed", "id"));
        ic_closed.setOnClickListener(v -> _closedActivity());
    }

    private void _changeOrientation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RTMPActivity.this);
        builder.setTitle("Select Orientation")
                .setItems(_orient, (dialog, which) -> {
                    camera1ApiManager.setPreviewOrientation(Integer.parseInt(_orient[which]));
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void _toggleStreaming() {
        if (!rtmpCameral.isStreaming()) {
            _startStreaming();
        } else {
            _stopStreaming();
        }
    }

    private void _startStreaming() {
        rtmpCameral.setAuthorization(_username, _password);
		
		int _w = 0;
		int _h = 0;

        this.resolutions = rtmpCameral.getResolutionsBack();
        for (int i = 0; i < this.resolutions.size(); i++) {
            Log.i(TAG, "RES: H: " + this.resolutions.get(i).height + " W: " + this.resolutions.get(i).width);
			
			// get the recommended resolution
			if (this.resolutions.get(i).width >= 720 && _w == 0) {
				_w = this.resolutions.get(i).width;
				_h = this.resolutions.get(i).height;
			}
        }

		if (_w == 0 && _h == 0) {
			_w = (this.selectedWidth == 0) ? this.resolutions.get(0).width : this.selectedWidth;
			_h = (this.selectedHeight == 0) ? this.resolutions.get(0).height : this.selectedHeight;
		}

        // video helper
        VideoHelper videoH = new VideoHelper();
        Log.d(TAG, "Res: " + _w + "x" + _h + " bitrate " + videoH.bitrate(_w, _h));

        if (rtmpCameral.prepareAudio() && rtmpCameral.prepareVideo(_w, _h, 30, videoH.bitrate(_w, _h),
                false, 0)) {
            rtmpCameral.startStream(_url);
            _toggleBtnImgVideo(true);

            VideoStream.sendBroadCast(activity, "onStartStream");
        } else {
            Toast.makeText(activity, "Error preparing stream, This device cant do it.", Toast.LENGTH_SHORT)
                    .show();
            _toggleBtnImgVideo(false);

            JSONObject obj = new JSONObject();
            try {
                obj.put("message", "Error preparing stream, This device cant do it.");
                VideoStream.sendBroadCast(activity, "onError", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void _stopStreaming() {
        VideoStream.sendBroadCast(activity, "onStopStream");

        if (rtmpCameral.isStreaming()) {
            // stop recording
            _stopRecording();

            rtmpCameral.stopStream();
            rtmpCameral.stopPreview();
            _toggleBtnImgVideo(false);

            // close the flash
            if (camera1ApiManager.isLanternEnabled()) {
                camera1ApiManager.disableLantern();

                isFlashOn = false;
                _toggleBtnImgFlash();
            }
        }
    }

    private void _toggleRecording() {
        if (folder == null) {
            Toast.makeText(activity.getApplicationContext(), "You device is not capable of recording.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (rtmpCameral.isStreaming()) {
            if (rtmpCameral.isRecording()) {
                _stopRecording();
            } else {
                try {
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
                    currentDateAndTime = sdf.format(new Date());
                    rtmpCameral.startRecord(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");

                    _toggleBtnRecord(true);
                } catch (IOException e) {
                    rtmpCameral.stopRecord();
                    _toggleBtnRecord(false);

                    e.printStackTrace();
                }
            }
        }
    }

    private void _stopRecording() {
        if (rtmpCameral.isRecording()) {
            try {
                _toggleBtnRecord(false);
                rtmpCameral.stopRecord();

                Toast.makeText(activity, "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                new VideoStream().scanFiles(activity.getApplicationContext(), new File(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4"));
                currentDateAndTime = "";
            } catch (Exception e) {
                Log.e(TAG, "_stopRecording: " + e.getMessage());
            }
        }
    }

    private void _toggleFlash() {
        if (!isFlashOn && !camera1ApiManager.isLanternEnabled()) {
            try {
                camera1ApiManager.enableLantern();
                isFlashOn = true;
            } catch (Exception e) {
                e.printStackTrace();
                isFlashOn = false;
            }
        } else {
            camera1ApiManager.disableLantern();
            isFlashOn = false;
        }

        // changing button/switch image
        _toggleBtnImgFlash();
    }

    private void _toggleBtnImgFlash() {
        String icon = (!isFlashOn) ? "ic_flash_on_white_36dp" : "ic_flash_off_white_36dp";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ic_torch.setImageDrawable(getDrawable(_getResource(icon, "drawable")));
        } else {
            ic_torch.setImageResource(_getResource(icon, "drawable"));
        }
    }

    private void _toggleBtnImgVideo(boolean isStreamingOn) {
        String icon = (!isStreamingOn) ? "ic_videocam_white_36dp" : "ic_videocam_off_white_36dp";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ic_broadcast.setImageDrawable(getDrawable(_getResource(icon, "drawable")));
        } else {
            ic_broadcast.setImageResource(_getResource(icon, "drawable"));
        }
    }

    private void _toggleBtnRecord(boolean isRecordingOn) {
        String icon = (!isRecordingOn) ? "ic_fiber_manual_record_white_36dp" : "ic_stop_white_36dp";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ic_record.setImageDrawable(getDrawable(_getResource(icon, "drawable")));
        } else {
            ic_record.setImageResource(_getResource(icon, "drawable"));
        }

        _animateRecording(icon);
    }

    private void _animateRecording(String icon) {
        if (icon.equals("ic_stop_white_36dp")) {
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(600);
            anim.setStartOffset(200);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            ic_record.startAnimation(anim);
        } else {
            ic_record.clearAnimation();
        }
    }

    private void _toggleCameraFace() {
        try {
            rtmpCameral.switchCamera();
        } catch (CameraOpenException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            rtmpCameral.switchCamera();
        }
    }

    private void _commentContainer(boolean isShow) {
        commentForm = findViewById(_getResource("commentForm", "id"));
        txtComment = findViewById(_getResource("txtComment", "id"));
        btnComment = findViewById(_getResource("btnComment", "id"));

        mComments = new ArrayList<>();
        adapter = new CommentListAdapter(this, _getResource("comment_list", "layout"), mComments);
        list = findViewById(_getResource("commentList", "id"));
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            // click on item
            _itemCommentSelected(position);
        });

        btnComment.setOnClickListener(v -> {
            // send comment
            isListShow = !isListShow;
            _isListCommentShow(isListShow);
        });

        txtComment.clearFocus();
        txtComment.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                _sendComment(v);
                return true;
            }

            return false;
        });

        _commentFormVisible(isShow);
    }

    private void _itemCommentSelected(int position) {
        JSONObject obj = new JSONObject();
        Comments getComment = mComments.get(position);

        try {
            obj.put("username", getComment.getTxtCommentUsername());
            obj.put("content", getComment.getTxtCommentContent());
            VideoStream.sendBroadCast(activity, "onCommentItemSelected", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void _sendComment(TextView v) {
        // send comment
        String comment = v.getText().toString();
        JSONObject obj = new JSONObject();
        try {
            obj.put("comment", comment);
            VideoStream.sendBroadCast(activity, "onCommentSend", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        txtComment.setText(null);
        txtComment.clearFocus();
    }

    private void _commentList(String data) {
        try {
            if (mComments != null) {
                mComments.clear();
            }

            if (adapter != null) {
                adapter.clear();
            }

            mComments = new ArrayList<>();
            JSONArray items = new JSONArray(data);

            for (int i = 0; i < items.length(); i++) {
                JSONObject object = items.getJSONObject(i);
                Comments comments = new Comments();
                comments.setImgCommentAvatar(object.getString("avatar"));
                comments.setTxtCommentUsername(object.getString("username"));
                comments.setTxtCommentContent(object.getString("content"));

                mComments.add(comments);
            }

            _updateCommentList(true, mComments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void _updateCommentList(boolean isShow, ArrayList<Comments> items) {
        runOnUiThread(() -> {
            _commentFormVisible(isShow);

            adapter.addAll(items);
            adapter.notifyDataSetChanged();
        });
    }

    private void _isListCommentShow(boolean isShow) {
        if (list == null) {
            return;
        }

        if (isShow) {
            list.setVisibility(View.VISIBLE);
        } else {
            list.setVisibility(View.INVISIBLE);
        }
    }

    private void _commentFormVisible(boolean isShow) {
        if (commentForm == null || list == null || txtComment == null || btnComment == null) {
            return;
        }

        if (isShow) {
            commentForm.setBackgroundColor(Color.parseColor("#40536077"));
            list.setVisibility(View.VISIBLE);
            txtComment.setVisibility(View.VISIBLE);
            btnComment.setVisibility(View.VISIBLE);
        } else {
            commentForm.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            list.setVisibility(View.INVISIBLE);
            txtComment.setVisibility(View.INVISIBLE);
            btnComment.setVisibility(View.INVISIBLE);
        }
    }

    private void _settingsDialog() {
        if (this.resolutions == null) {
            if (rtmpCameral == null) {
                return;
            }

            this.resolutions = rtmpCameral.getResolutionsBack();
        }

        String[] resolutions = new String[this.resolutions.size()];
        for (int i = 0; i < this.resolutions.size(); i++) {
            resolutions[i] = this.resolutions.get(i).width + "x" + this.resolutions.get(i).height;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change resolution");
        builder.setItems(resolutions, (dialog, which) -> {
            // the user clicked on colors[which]
            Log.d(TAG, "Selected: " + resolutions[which] + " " +
                    this.resolutions.get(which).width + "x" + this.resolutions.get(which).height);

            this.selectedWidth = this.resolutions.get(which).width;
            this.selectedHeight = this.resolutions.get(which).height;

            if (this.rtmpCameral.isStreaming()) {
                this._stopStreaming();

                new android.os.Handler().postDelayed(this::_startStreaming, 1000);
            }
        });
        builder.show();
    }

    private void _closedActivity() {
        this._stopStreaming();
        finish();
    }

    private int _getResource(String name, String type) {
        String package_name = getApplication().getPackageName();
        Resources resources = getApplication().getResources();
        return resources.getIdentifier(name, type, package_name);
    }
}
