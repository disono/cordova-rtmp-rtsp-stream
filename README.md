# Cordova RTMP and RTSP Streamer
Library based on [rtmp-rtsp-stream-client-java](https://github.com/pedroSG94/rtmp-rtsp-stream-client-java)

# Install
Latest stable version from npm:
```sh
$ cordova plugin add cordova-rtsp-rtmp-stream
```
Bleeding edge version from Github:
```sh
$ cordova plugin add https://github.com/disono/cordova-rtmp-rtsp-stream
```

# Using the plugin
```sh
videoStreamer.streamRTSPAuth('rtsp://your-ip/live/app_name', 'your-username', 'your-password', [success], [failed]);
videoStreamer.streamStop([success], [failed]);
```

# Methods
```sh
videoStreamer.streamRTSP('uri', [success], [failed]);
videoStreamer.streamRTMP('uri', [success], [failed]);

videoStreamer.streamRTMPAuth('uri', 'your-username', 'your-password', [success], [failed]);
videoStreamer.streamRTSPAuth('uri', 'your-username', 'your-password', [success], [failed]);

// manually stop streaming
videoStreamer.streamStop([success], [failed]);

// comment list (Optional)
videoStreamer.commentListShow(boolean, [success], [failed]);
videoStreamer.commentList([{avatar: 'URI', username: 'Username', content: 'Message/Comment/Content Text'}], [success], [failed]);

// record locally (MP4)
videoStreamer.videoRecord([success], [failed]);
```

# RTMP & RTSP action response
```
onConnectionSuccess: data

onConnectionFailed: void

onDisconnect: void

onAuthError: void

onAuthSuccess: data

onStartStream: data

onStopStream: data

onError: data

onCommentSend: data

onCommentItemSelected: data
```

# Future Features
- [ ] iOS support using HaishinKit.swift (Separate Plugin - RTMP Only)
[https://github.com/disono/cordova-rtmp-ios](https://github.com/disono/cordova-rtmp-ios)

- [x] Support for comment form and comment list (Android)
- [x] Comment form

# Credits
[https://github.com/pedroSG94/rtmp-rtsp-stream-client-java](https://github.com/pedroSG94/rtmp-rtsp-stream-client-java)
```sh
Library for stream in RTMP and RTSP. All code in Java.
```

# License
Cordova RTMP and RTSP Streamer is licensed under the Apache License (ASL) license. For more information, see the LICENSE file in this repository.