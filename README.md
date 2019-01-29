# Cordova RTMP and RTSP Live Streaming Plugin
This cordova plugin is use to stream a live video to RTMP or RTSP server.
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
// without comment
videoStreamer.streamRTSPAuth('rtsp://your-ip/live/app_name', 'your-username', 'your-password', [success], [failed]);
videoStreamer.streamStop([success], [failed]);

// show comment
videoStreamer.streamRTSPAuth('rtsp://your-ip/live/app_name', 'your-username', 'your-password', function (res) 
	// show all the comment form and list if connection and authentication is successful
	if (res.event_name == 'onConnectionSuccess') {
		videoStreamer.commentListShow(true, function (resCom) {
					
		}, function (e) {
					
		});
	}
}, function (e) {
		
});
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

# Hire Us!
Do you like my work? I am a Senior Full Stack App Developer (Web, Desktop & Mobile App) & Syadmin, you can hire me, open a issue or contact me!

Additionally you could be interested in check or hire us through our [Webmons Development Studio.](https://webmons.com)

Do you still need more info about me or my work?

You could check my [Linkedin profile](https://www.linkedin.com/in/disono) and [Twitter profile](https://twitter.com/master_archie)