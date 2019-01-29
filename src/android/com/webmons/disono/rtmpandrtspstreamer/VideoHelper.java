package com.webmons.disono.rtmpandrtspstreamer;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: http://www.webmons.com
 *
 * Created at: 11/15/2018
 */

public class VideoHelper {
    public int bitrate(int width, int height) {
        if (width <= 640 && height <= 360) {
            return 560;
        }

        if (width <= 854 && height <= 480) {
            return 1200;
        }

        if (width <= 1280 && height <= 720) {
            return 2000;
        }

        if (width <= 1920 && height <= 1080) {
            return 6000;
        }

        return 1200;
    }
}
