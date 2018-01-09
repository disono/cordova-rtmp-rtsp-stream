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
```

# Credits
[https://github.com/pedroSG94/rtmp-rtsp-stream-client-java](https://github.com/pedroSG94/rtmp-rtsp-stream-client-java)
```sh
Library for stream in RTMP and RTSP. All code in Java.
```

# License
Cordova RTMP and RTSP Streamer is licensed under the Apache License (ASL) license. For more information, see the LICENSE file in this repository.